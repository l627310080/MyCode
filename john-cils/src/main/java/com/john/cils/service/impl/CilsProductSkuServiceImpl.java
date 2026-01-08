package com.john.cils.service.impl;

import com.john.cils.domain.CilsProductSku;
import com.john.cils.mapper.CilsProductSkuMapper;
import com.john.cils.service.ICilsProductSkuService;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 商品单品规格表(SKU)Service业务层处理
 * <p>
 * 引入 Redis 实现高并发库存管理。
 * 策略：
 * 1. 库存数据在 Redis 中永久保存，作为权威数据源。
 * 2. 使用 Redis 的原子操作 (DECRBY) 来安全地扣减库存。
 * 3. 增、删、改操作同时维护数据库和 Redis 的数据一致性。
 *
 * @author john
 * @date 2024-05-20
 */
@Service
public class CilsProductSkuServiceImpl implements ICilsProductSkuService {

    private static final Logger log = LoggerFactory.getLogger(CilsProductSkuServiceImpl.class);
    // 定义库存缓存键的前缀，例如：inventory:sku:1001
    private static final String INVENTORY_KEY_PREFIX = "inventory:sku:";
    // 定义详情缓存键的前缀，例如：sku_detail:1001
    private static final String DETAIL_KEY_PREFIX = "sku_detail:";
    @Autowired
    private CilsProductSkuMapper cilsProductSkuMapper;
    @Autowired
    private RedisCache redisCache;

    /**
     * 查询商品单品规格表(SKU)
     * <p>
     * 优化逻辑：
     * 1. 先查 Redis 详情缓存 (防止页面加载崩库)。
     * 2. 如果没有，查数据库，并回写 Redis 详情缓存。
     * 3. 无论详情从哪来，库存都要单独查 Redis 库存缓存 (保证库存实时性)。
     */
    @Override
    public CilsProductSku selectCilsProductSkuById(Long id) {
        String detailKey = DETAIL_KEY_PREFIX + id;

        // 1. 尝试从 Redis 获取商品详情 (描述、价格等)
        CilsProductSku sku = redisCache.getCacheObject(detailKey);

        if (sku == null) {
            // 2. 缓存未命中，查询数据库
            sku = cilsProductSkuMapper.selectCilsProductSkuById(id);
            if (sku != null) {
                // 回写详情缓存，设置 1 小时过期
                redisCache.setCacheObject(detailKey, sku, 1, TimeUnit.HOURS);
            }
        }

        if (sku == null) {
            return null;
        }

        // 3. 覆盖实时库存
        // 详情缓存里的库存可能是旧的，必须从库存专用 Key 获取最新值
        String inventoryKey = INVENTORY_KEY_PREFIX + id;
        Long stockQty = redisCache.getCacheObject(inventoryKey);

        // 如果 Redis 没有库存信息（可能服务重启或新商品），从数据库加载并回写
        if (stockQty == null) {
            stockQty = sku.getStockQty() != null ? sku.getStockQty() : 0L;
            redisCache.setCacheObject(inventoryKey, stockQty);
        }

        // 4. 页面美化：如果库存为负数（高并发回滚期间），展示库存为 0
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

    /**
     * 新增商品单品规格表(SKU)
     */
    @Override
    @Transactional
    public int insertCilsProductSku(CilsProductSku cilsProductSku) {
        cilsProductSku.setCreateTime(DateUtils.getNowDate());
        int rows = cilsProductSkuMapper.insertCilsProductSku(cilsProductSku);
        if (rows > 0) {
            // 将初始库存写入 Redis (永久有效)
            String inventoryKey = INVENTORY_KEY_PREFIX + cilsProductSku.getId();
            Long stockQty = cilsProductSku.getStockQty() != null ? cilsProductSku.getStockQty() : 0L;
            redisCache.setCacheObject(inventoryKey, stockQty);
        }
        return rows;
    }

    /**
     * 修改商品单品规格表(SKU)
     */
    @Override
    @Transactional
    public int updateCilsProductSku(CilsProductSku cilsProductSku) {
        int rows = cilsProductSkuMapper.updateCilsProductSku(cilsProductSku);

        if (rows > 0 && cilsProductSku.getId() != null) {
            // 1. 删除详情缓存 (保证下次查询能取到最新描述/价格)
            redisCache.deleteObject(DETAIL_KEY_PREFIX + cilsProductSku.getId());

            // 2. 如果修改了库存，同步更新 Redis 库存缓存
            if (cilsProductSku.getStockQty() != null) {
                String inventoryKey = INVENTORY_KEY_PREFIX + cilsProductSku.getId();
                redisCache.setCacheObject(inventoryKey, cilsProductSku.getStockQty());
            }
        }
        return rows;
    }

    /**
     * 批量删除商品单品规格表(SKU)
     */
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

    /**
     * 删除商品单品规格表(SKU)信息
     */
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

    /**
     * (高并发)扣减SKU库存
     * <p>
     * 核心逻辑：
     * 1. 使用 Redis 原子递减。
     * 2. 判断递减后的结果。
     * 3. 如果库存归零，触发下架逻辑。
     * 4. 如果库存不足，回滚 Redis 数据。
     */
    @Override
    public boolean deductStock(Long skuId, Integer quantity) {
        String inventoryKey = INVENTORY_KEY_PREFIX + skuId;

        // 原子扣减：先斩后奏，保证并发安全
        Long remainingStock = redisCache.decrBy(inventoryKey, quantity);

        if (remainingStock >= 0) {
            // 扣减成功

            // 1. 异步更新数据库 (这里暂时同步执行)
            CilsProductSku skuToUpdate = new CilsProductSku();
            skuToUpdate.setId(skuId);
            skuToUpdate.setStockQty(remainingStock);
            cilsProductSkuMapper.updateCilsProductSku(skuToUpdate);

            // 2. 检查是否需要下架
            if (remainingStock == 0) {
                triggerOffShelf(skuId);
            }

            return true;
        } else {
            // 库存不足 (remainingStock < 0)
            // 回滚：把刚才扣的加回去
            redisCache.incrBy(inventoryKey, quantity);
            log.warn("库存不足，扣减失败。SKU: {}", skuId);
            return false;
        }
    }

    /**
     * 触发商品下架逻辑 (伪代码)
     */
    private void triggerOffShelf(Long skuId) {
        log.info("库存归零，触发下架逻辑。SKU: {}", skuId);
        // TODO: 调用外部平台 API 下架商品
        // platformService.offShelf(skuId, "AMAZON");
        // platformService.offShelf(skuId, "SHOPEE");

        // TODO: 更新本地 SPU/SKU 状态为 "已售罄"
    }
}
