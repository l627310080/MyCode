package com.john.cils.service;

import com.john.cils.domain.CilsPlatformMapping;
import com.john.cils.domain.CilsProductSku;
import com.john.cils.domain.CilsProductSpu;
import com.john.cils.mapper.CilsPlatformMappingMapper;
import com.john.cils.mapper.CilsProductSkuMapper;
import com.john.cils.mapper.CilsProductSpuMapper;
import com.john.cils.service.push.IPushStrategy;
import com.john.cils.utils.platform.AmazonPushUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 平台推送服务
 *
 * @author john
 * @date 2026-01-09
 */
@Service
public class PlatformPushService {

    private static final Logger log = LoggerFactory.getLogger(PlatformPushService.class);

    private final Map<String, IPushStrategy> strategyMap = new ConcurrentHashMap<>();

    @Autowired
    private CilsPlatformMappingMapper mappingMapper;

    @Autowired
    private CilsProductSkuMapper skuMapper;

    @Autowired
    private CilsProductSpuMapper spuMapper;

    @Autowired
    private AiTranslationService translationService;

    @Autowired
    public PlatformPushService(List<IPushStrategy> strategies) {
        strategies.forEach(strategy -> {
            strategyMap.put(strategy.getPlatformType(), strategy);
            log.info("已加载推送策略: {}", strategy.getPlatformType());
        });
    }

    /**
     * 推送商品到平台 (异步执行)
     */
    @Async
    @Retryable(value = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public void pushProduct(Long mappingId) {
        log.info(">>>> [演示] 开始执行推送任务，Mapping ID: {}", mappingId);

        CilsPlatformMapping mapping = mappingMapper.selectCilsPlatformMappingById(mappingId);
        if (mapping == null) {
            log.error("推送失败：找不到映射记录，ID: {}", mappingId);
            return;
        }

        // --- 状态守卫：只有处于“待同步(0)”状态的记录才允许执行铺货流程 ---
        if (mapping.getSyncStatus() != 0L) {
            log.warn("拦截异步推送：Mapping ID {} 当前状态为 {}, 非待同步状态(0), 操作取消", mappingId, mapping.getSyncStatus());
            return;
        }

        String platform = mapping.getPlatformType();
        log.info("目标平台: {}, 目标国家: {}", platform, mapping.getTargetCountry());

        IPushStrategy strategy = strategyMap.get(platform);
        if (strategy == null) {
            log.error("推送失败：暂不支持该平台类型: {}", platform);
            updateMappingStatus(mapping, 2L, "暂不支持该平台类型: " + platform);
            return;
        }

        // --- 新增：AI 多语言翻译 ---
        try {
            // 1. 获取源文本 (SPU标题 + SKU规格)
            CilsProductSku sku = skuMapper.selectCilsProductSkuById(mapping.getSkuId());
            CilsProductSpu spu = spuMapper.selectCilsProductSpuById(sku.getSpuId());

            String sourceText = spu.getProductName() + " - " + sku.getSpecInfo();

            // 2. 确定目标语言
            String targetLanguage = "English";
            if ("TH".equalsIgnoreCase(mapping.getTargetCountry()))
                targetLanguage = "Thai";
            else if ("JP".equalsIgnoreCase(mapping.getTargetCountry()))
                targetLanguage = "Japanese";

            // 3. 执行翻译
            String translatedText = translationService.translate(sourceText, targetLanguage);
            log.info("翻译结果: {}", translatedText);

            // 4. 直接拼接翻译结果到备注中
            String oldRemark = mapping.getRemark() != null ? mapping.getRemark() : "";
            mapping.setRemark(oldRemark + " " + translatedText);

        } catch (Exception e) {
            log.warn("翻译过程中发生异常，将使用原文推送", e);
        }

        // --- 执行推送 ---
        try {
            boolean success = strategy.push(mapping);

            if (success) {
                log.info("<<< 推送成功！更新同步状态为已同步");
                String platformItemId = "ITEM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                mapping.setPlatformItemId(platformItemId);
                updateMappingStatus(mapping, 1L, null);
            } else {
                log.error("<<< 推送失败！请检查网络或平台配置");
                updateMappingStatus(mapping, 2L, "推送失败，请检查网络或平台配置");
            }
        } catch (Exception e) {
            log.warn(">>>> [演示] 推送过程中捕捉到异常: {}. 准备触发 Spring Retry 自动重试...", e.getMessage());
            // 如果是因为网络超时抛出的 RuntimeException，我们将其抛出给 @Retryable 处理
            throw new RuntimeException(e);
        }
    }

    private void updateMappingStatus(CilsPlatformMapping mapping, Long status, String failReason) {
        mapping.setSyncStatus(status);
        mapping.setSyncFailReason(failReason);
        log.info("更新 Mapping {} 状态: status={}, remark={}", mapping.getId(), status, mapping.getRemark());
        mappingMapper.updateCilsPlatformMapping(mapping);
    }

    @Async
    public void syncStock(Long skuId) {
        // 做压测，日志暂时关闭
//        log.info(">>> 开始库存到外部平台，SKU ID: {}", skuId);

        CilsPlatformMapping query = new CilsPlatformMapping();
        query.setSkuId(skuId);
        List<CilsPlatformMapping> mappings = mappingMapper.selectCilsPlatformMappingList(query);

        if (mappings.isEmpty()) {
            log.info("该 SKU 未关联任何平台，无需同步");
            return;
        }

        for (CilsPlatformMapping mapping : mappings) {
            // --- 状态守卫：只有处于“待同步(0)”或“已同步(1)”状态的记录才允许同步库存 ---
            // 处于“待校验(3)”状态的记录严禁同步库存到外部平台
            if (mapping.getSyncStatus() != 0L && mapping.getSyncStatus() != 1L) {
                // 这里做压测，日志暂时关闭
//                log.warn("拦截库存同步：Mapping ID {} 处于非铺货状态 ({}), 严禁库存下发", mapping.getId(), mapping.getSyncStatus());
                continue;
            }

            // 这里没有调用真实接口，只是写一个示例
            if ("AMAZON".equalsIgnoreCase(mapping.getPlatformType())) {
                // 这里做压测，日志暂时关闭
//                log.info("正在同步 Amazon 库存，Platform SKU: {}", mapping.getPlatformSku());
                AmazonPushUtils.syncStock(mapping.getPlatformSku(), 99L);
            }
        }

        // 这里做压测，日志暂时关闭
//        log.info("<<< 库存同步任务完成");
    }
}
