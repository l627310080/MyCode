package com.john.cils.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.john.cils.common.constant.CilsConstants;
import com.john.cils.domain.CilsPlatformMapping;
import com.john.cils.domain.CilsProductSku;
import com.john.cils.domain.dto.TargetMarketDTO;
import com.john.cils.mapper.CilsPlatformMappingMapper;
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
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    private CilsPlatformMappingMapper cilsPlatformMappingMapper;

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

    private static final String INVENTORY_KEY_PREFIX = "inventory:sku:";
    private static final String DETAIL_KEY_PREFIX = "sku_detail:";
    private static final String TOPIC_STOCK_DEDUCT = "stock-deduct-topic";

    /**
     * 根据ID查询SKU详情，采用 Cache-Aside 缓存模式
     * 同时查询关联的平台映射信息，用于前端回显
     * 
     * @param id SKU主键ID
     * @return SKU领域对象
     */
    @Override
    public CilsProductSku selectCilsProductSkuById(Long id) {
        String detailKey = DETAIL_KEY_PREFIX + id;

        // 1. 尝试从 Redis 获取
        Object cacheObj = redisCache.getCacheObject(detailKey);
        CilsProductSku sku = null;

        // 2. 类型检查与转换
        if (cacheObj instanceof CilsProductSku) {
            sku = (CilsProductSku) cacheObj;
        } else if (cacheObj != null) {
            log.warn("Redis缓存类型不匹配，Key: {}, 实际类型: {}", detailKey, cacheObj.getClass().getName());
            redisCache.deleteObject(detailKey);
        }

        // 3. 缓存未命中或类型不匹配，查数据库
        if (sku == null) {
            sku = cilsProductSkuMapper.selectCilsProductSkuById(id);
            if (sku != null) {
                redisCache.setCacheObject(detailKey, sku, 1, TimeUnit.HOURS);
            }
        }

        if (sku == null)
            return null;

        String inventoryKey = INVENTORY_KEY_PREFIX + id;
        Long stockQty = redisCache.getCacheObject(inventoryKey);

        if (stockQty == null) {
            stockQty = sku.getStockQty() != null ? sku.getStockQty() : 0L;
            redisCache.setCacheObject(inventoryKey, stockQty);
        }

        if (stockQty < 0)
            stockQty = 0L;
        sku.setStockQty(stockQty);

        // --- 恢复：查询关联的 Mapping 并转换为 DTO ---
        CilsPlatformMapping query = new CilsPlatformMapping();
        query.setSkuId(id);
        List<CilsPlatformMapping> mappings = cilsPlatformMappingMapper.selectCilsPlatformMappingList(query);

        if (mappings != null && !mappings.isEmpty()) {
            List<TargetMarketDTO> markets = mappings.stream().map(m -> {
                TargetMarketDTO dto = new TargetMarketDTO();
                dto.setId(m.getId());
                dto.setPlatform(m.getPlatformType());
                dto.setCountry(m.getTargetCountry());
                dto.setPrice(m.getSalePrice());
                dto.setCurrency(m.getCurrency());
                dto.setPriceMode(CilsConstants.PRICE_MODE_FIXED);
                dto.setInputValue(m.getSalePrice());
                return dto;
            }).collect(Collectors.toList());

            sku.setTargetMarkets(markets);
        }

        return sku;
    }

    @Override
    public List<CilsProductSku> selectCilsProductSkuList(CilsProductSku cilsProductSku) {
        return cilsProductSkuMapper.selectCilsProductSkuList(cilsProductSku);
    }

    @Override
    @Transactional
    public int insertCilsProductSku(CilsProductSku cilsProductSku) {
        log.info(">>> 开始新增SKU, SPU ID: {}", cilsProductSku.getSpuId());
        if (StringUtils.isEmpty(cilsProductSku.getSkuCode())) {
            String code = "SKU-" + cilsProductSku.getSpuId() + "-" + System.currentTimeMillis() % 1000000;
            cilsProductSku.setSkuCode(code);
            log.info("自动生成SKU编码: {}", code);
        }

        cilsProductSku.setCreateTime(DateUtils.getNowDate());
        cilsProductSku.setIsAudit(CilsConstants.AUDIT_STATUS_WAITING);

        int rows = cilsProductSkuMapper.insertCilsProductSku(cilsProductSku);
        log.info("数据库插入SKU操作完成, 受影响行数: {}", rows);

        if (rows > 0) {
            Long stockQty = cilsProductSku.getStockQty() != null ? cilsProductSku.getStockQty() : 0L;
            String inventoryKey = INVENTORY_KEY_PREFIX + cilsProductSku.getId();
            redisCache.setCacheObject(inventoryKey, stockQty);
            log.info(">>> Redis库存预热成功: key={}, value={}", inventoryKey, stockQty);

            autoCreateMappings(cilsProductSku);

            asyncVerificationService.triggerVerification(cilsProductSku);
        } else {
            log.error("数据库插入SKU失败, 未执行Redis预热和异步校验");
        }
        return rows;
    }

    private void autoCreateMappings(CilsProductSku sku) {
        List<TargetMarketDTO> markets = sku.getTargetMarkets();
        if (markets == null || markets.isEmpty())
            return;

        log.info(">>> 开始为SKU {} 创建 {} 条平台映射", sku.getId(), markets.size());
        for (TargetMarketDTO market : markets) {
            CilsPlatformMapping mapping = new CilsPlatformMapping();
            mapping.setSkuId(sku.getId());
            mapping.setPlatformType(market.getPlatform());
            mapping.setTargetCountry(market.getCountry());
            mapping.setPlatformSku(sku.getSkuCode());
            mapping.setSalePrice(market.getPrice());

            String currency = "USD";
            if ("UK".equals(market.getCountry()))
                currency = "GBP";
            if ("TH".equals(market.getCountry()))
                currency = "THB";
            mapping.setCurrency(currency);

            mapping.setSyncStatus(CilsConstants.SYNC_STATUS_WAIT_VERIFY);

            mappingService.insertCilsPlatformMapping(mapping);
        }
        log.info("<<< 平台映射创建完成");
    }

    /**
     * 更新SKU信息，包含关联 Mapping 的 Diff 更新
     * 
     * @param cilsProductSku 待更新的SKU对象
     * @return 数据库受影响行数
     */
    @Override
    @Transactional
    public int updateCilsProductSku(CilsProductSku cilsProductSku) {
        cilsProductSku.setIsAudit(CilsConstants.AUDIT_STATUS_WAITING);
        cilsProductSku.setRemark("");

        int rows = cilsProductSkuMapper.updateCilsProductSku(cilsProductSku);

        if (rows > 0 && cilsProductSku.getId() != null) {
            // 清理详情缓存
            redisCache.deleteObject(DETAIL_KEY_PREFIX + cilsProductSku.getId());

            // 更新库存缓存
            if (cilsProductSku.getStockQty() != null) {
                String inventoryKey = INVENTORY_KEY_PREFIX + cilsProductSku.getId();
                redisCache.setCacheObject(inventoryKey, cilsProductSku.getStockQty());
                log.info(">>> Redis库存更新成功: key={}, value={}", inventoryKey, cilsProductSku.getStockQty());
                // 注意：这里不再调用同步库存，必须等 AI 校验通过
            }

            // 1. 先更新/重置平台映射状态为 WAIT_VERIFY
            updateMappings(cilsProductSku);

            // 2. 触发异步 AI 校验（校验通过后会在异步线程自动同步库存）
            asyncVerificationService.triggerVerification(cilsProductSku);
        }
        return rows;
    }

    /**
     * 智能更新关联的 Mapping (Diff 算法)
     * 
     * @param sku 包含最新 Mapping 列表的 SKU 对象
     */
    private void updateMappings(CilsProductSku sku) {
        List<TargetMarketDTO> newMarkets = sku.getTargetMarkets();
        if (newMarkets == null)
            return;

        log.info(">>> 开始处理 SKU {} 的 Mapping 更新", sku.getId());

        CilsPlatformMapping query = new CilsPlatformMapping();
        query.setSkuId(sku.getId());
        List<CilsPlatformMapping> existingMappings = cilsPlatformMappingMapper.selectCilsPlatformMappingList(query);

        List<Long> newIds = newMarkets.stream()
                .map(TargetMarketDTO::getId)
                .filter(id -> id != null)
                .collect(Collectors.toList());

        List<Long> idsToDelete = existingMappings.stream()
                .map(CilsPlatformMapping::getId)
                .filter(id -> !newIds.contains(id))
                .collect(Collectors.toList());

        if (!idsToDelete.isEmpty()) {
            log.info("检测到需删除的 Mapping ID: {}", idsToDelete);
            cilsPlatformMappingMapper.deleteCilsPlatformMappingByIds(idsToDelete.toArray(new Long[0]));
        }

        for (TargetMarketDTO market : newMarkets) {
            CilsPlatformMapping mapping = new CilsPlatformMapping();
            mapping.setSkuId(sku.getId());
            mapping.setPlatformType(market.getPlatform());
            mapping.setTargetCountry(market.getCountry());
            mapping.setPlatformSku(sku.getSkuCode());
            mapping.setSalePrice(market.getPrice());

            String currency = "USD";
            if ("UK".equals(market.getCountry()))
                currency = "GBP";
            if ("TH".equals(market.getCountry()))
                currency = "THB";
            mapping.setCurrency(currency);

            if (market.getId() == null) {
                log.info("新增 Mapping: {} - {}", market.getPlatform(), market.getCountry());
                mapping.setSyncStatus(CilsConstants.SYNC_STATUS_WAIT_VERIFY);
                mappingService.insertCilsPlatformMapping(mapping);
            } else {
                log.info("更新 Mapping ID: {}", market.getId());
                mapping.setId(market.getId());
                mapping.setSyncStatus(CilsConstants.SYNC_STATUS_WAIT_VERIFY);
                cilsPlatformMappingMapper.updateCilsPlatformMapping(mapping);
            }
        }
        log.info("<<< Mapping 更新处理完成");
    }

    @Override
    @Transactional
    public int deleteCilsProductSkuByIds(Long[] ids) {
        int rows = cilsProductSkuMapper.deleteCilsProductSkuByIds(ids);
        if (rows > 0) {
            for (Long id : ids) {
                CilsPlatformMapping query = new CilsPlatformMapping();
                query.setSkuId(id);
                List<CilsPlatformMapping> mappings = cilsPlatformMappingMapper.selectCilsPlatformMappingList(query);
                if (!mappings.isEmpty()) {
                    List<Long> mappingIds = mappings.stream().map(CilsPlatformMapping::getId)
                            .collect(Collectors.toList());
                    cilsPlatformMappingMapper.deleteCilsPlatformMappingByIds(mappingIds.toArray(new Long[0]));
                }

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
            CilsPlatformMapping query = new CilsPlatformMapping();
            query.setSkuId(id);
            List<CilsPlatformMapping> mappings = cilsPlatformMappingMapper.selectCilsPlatformMappingList(query);
            if (!mappings.isEmpty()) {
                List<Long> mappingIds = mappings.stream().map(CilsPlatformMapping::getId).collect(Collectors.toList());
                cilsPlatformMappingMapper.deleteCilsPlatformMappingByIds(mappingIds.toArray(new Long[0]));
            }

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
            log.info(">>> Redis 扣减成功，SKU: {}, 剩余库存: {}", skuId, remainingStock);

            JSONObject message = new JSONObject();
            message.put("skuId", skuId);
            message.put("remainingStock", remainingStock);

            try {
                kafkaTemplate.send(TOPIC_STOCK_DEDUCT, message.toJSONString());
                log.info(">>> Kafka 消息发送成功: {}", message);
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
