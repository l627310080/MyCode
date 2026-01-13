package com.john.cils.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.john.cils.common.constant.CilsConstants;
import com.john.cils.domain.CilsPlatformMapping;
import com.john.cils.domain.CilsProductSku;
import com.john.cils.domain.dto.TargetMarketDTO;
import com.john.cils.mapper.CilsProductSkuMapper;
import com.john.cils.service.ExchangeRateService;
import com.john.cils.service.ICilsPlatformMappingService;
import com.john.cils.service.ICilsProductSkuService;
import com.john.cils.service.PlatformPushService;
import com.john.cils.verification.async.AsyncVerificationService;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 商品单品规格表(SKU)Service业务层处理
 *
 * @author john
 * @date 2024-05-20
 */
@Service
public class CilsProductSkuServiceImpl implements ICilsProductSkuService {
    
    private static final Logger log = LoggerFactory.getLogger(CilsProductSkuServiceImpl.class);

    @Autowired
    private CilsProductSkuMapper cilsProductSkuMapper;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private AsyncVerificationService asyncVerificationService;
    
    @Autowired
    private PlatformPushService platformPushService;
    
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ICilsPlatformMappingService mappingService;

    @Autowired
    private ExchangeRateService exchangeRateService;

    @Value("${cils.price.multiplier:1.5}")
    private String defaultMultiplier;

    private static final String INVENTORY_KEY_PREFIX = "inventory:sku:";
    private static final String DETAIL_KEY_PREFIX = "sku_detail:";
    private static final String TOPIC_STOCK_DEDUCT = "stock-deduct-topic";

    @Override
    public CilsProductSku selectCilsProductSkuById(Long id) {
        String detailKey = DETAIL_KEY_PREFIX + id;
        CilsProductSku sku = redisCache.getCacheObject(detailKey);
        
        if (sku == null) {
            sku = cilsProductSkuMapper.selectCilsProductSkuById(id);
            if (sku != null) {
                redisCache.setCacheObject(detailKey, sku, 1, TimeUnit.HOURS);
            }
        }
        
        if (sku == null) {
            return null;
        }

        String inventoryKey = INVENTORY_KEY_PREFIX + id;
        Long stockQty = redisCache.getCacheObject(inventoryKey);

        if (stockQty == null) {
            stockQty = sku.getStockQty() != null ? sku.getStockQty() : 0L;
            redisCache.setCacheObject(inventoryKey, stockQty);
        }

        if (stockQty < 0) {
            stockQty = 0L;
        }

        sku.setStockQty(stockQty);
        return sku;
    }

    @Override
    public List<CilsProductSku> selectCilsProductSkuList(CilsProductSku cilsProductSku) {
        return cilsProductSkuMapper.selectCilsProductSkuList(cilsProductSku);
    }

    @Override
    @Transactional 
    public int insertCilsProductSku(CilsProductSku cilsProductSku) {
        // 自动生成 SKU 编码
        if (StringUtils.isEmpty(cilsProductSku.getSkuCode())) {
            String code = "SKU-" + cilsProductSku.getSpuId() + "-" + System.currentTimeMillis() % 1000000;
            cilsProductSku.setSkuCode(code);
        }

        cilsProductSku.setCreateTime(DateUtils.getNowDate());
        cilsProductSku.setIsAudit(CilsConstants.AUDIT_STATUS_WAITING); // 默认待审核

        int rows = cilsProductSkuMapper.insertCilsProductSku(cilsProductSku);
        
        if (rows > 0) {
            String inventoryKey = INVENTORY_KEY_PREFIX + cilsProductSku.getId();
            Long stockQty = cilsProductSku.getStockQty() != null ? cilsProductSku.getStockQty() : 0L;
            redisCache.setCacheObject(inventoryKey, stockQty);
            
            asyncVerificationService.triggerVerification(cilsProductSku);

            autoCreateMappings(cilsProductSku);
        }
        return rows;
    }

    private void autoCreateMappings(CilsProductSku sku) {
        List<TargetMarketDTO> markets = sku.getTargetMarkets();
        if (markets == null || markets.isEmpty()) {
            return;
        }

        for (TargetMarketDTO market : markets) {
            CilsPlatformMapping mapping = new CilsPlatformMapping();
            mapping.setSkuId(sku.getId());
            mapping.setPlatformType(market.getPlatform());
            mapping.setTargetCountry(market.getCountry());
            mapping.setPlatformSku(sku.getSkuCode()); 
            mapping.setSalePriceUsd(market.getPrice());
            mapping.setSyncStatus(0L); 

            mappingService.insertCilsPlatformMapping(mapping);
            
            platformPushService.pushProduct(mapping.getId());
        }
    }

    @Override
    @Transactional
    public int updateCilsProductSku(CilsProductSku cilsProductSku) {
        int rows = cilsProductSkuMapper.updateCilsProductSku(cilsProductSku);
        
        if (rows > 0 && cilsProductSku.getId() != null) {
            redisCache.deleteObject(DETAIL_KEY_PREFIX + cilsProductSku.getId());
            
            if (cilsProductSku.getStockQty() != null) {
                String inventoryKey = INVENTORY_KEY_PREFIX + cilsProductSku.getId();
                redisCache.setCacheObject(inventoryKey, cilsProductSku.getStockQty());
                platformPushService.syncStock(cilsProductSku.getId());
            }
            
            asyncVerificationService.triggerVerification(cilsProductSku);
        }
        return rows;
    }

    @Override
    @Transactional
    public int deleteCilsProductSkuByIds(Long[] ids) {
        int rows = cilsProductSkuMapper.deleteCilsProductSkuByIds(ids);
        if (rows > 0) {
            for (Long id : ids) {
                redisCache.deleteObject(INVENTORY_KEY_PREFIX + id);
                redisCache.deleteObject(DETAIL_KEY_PREFIX + id);
            }
        }
        return rows;
    }

    @Override
    @Transactional
    public int deleteCilsProductSkuById(Long id) {
        int rows = cilsProductSkuMapper.deleteCilsProductSkuById(id);
        if (rows > 0) {
            redisCache.deleteObject(INVENTORY_KEY_PREFIX + id);
            redisCache.deleteObject(DETAIL_KEY_PREFIX + id);
        }
        return rows;
    }

    @Override
    public boolean deductStock(Long skuId, Integer quantity) {
        String inventoryKey = INVENTORY_KEY_PREFIX + skuId;
        Long remainingStock = redisCache.decrBy(inventoryKey, quantity);

        if (remainingStock >= 0) {
            JSONObject message = new JSONObject();
            message.put("skuId", skuId);
            message.put("remainingStock", remainingStock);
            
            try {
                kafkaTemplate.send(TOPIC_STOCK_DEDUCT, message.toJSONString());
                log.info("库存扣减成功，已发送 Kafka 消息: {}", message);
                platformPushService.syncStock(skuId);
            } catch (Exception e) {
                log.error("Kafka 发送失败，回滚 Redis 库存", e);
                redisCache.incrBy(inventoryKey, quantity);
                return false;
            }
            
            if (remainingStock == 0) {
                triggerOffShelf(skuId);
            }
            return true;
        } else {
            redisCache.incrBy(inventoryKey, quantity);
            log.warn("库存不足，扣减失败。SKU: {}", skuId);
            return false;
        }
    }

    private void triggerOffShelf(Long skuId) {
        log.info("库存归零，触发下架逻辑。SKU: {}", skuId);
    }
}
