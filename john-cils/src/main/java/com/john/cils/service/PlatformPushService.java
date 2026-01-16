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
    public void pushProduct(Long mappingId) {
        log.info(">>> 准备执行异步推送任务，Mapping ID: {}", mappingId);
        
        CilsPlatformMapping mapping = mappingMapper.selectCilsPlatformMappingById(mappingId);
        if (mapping == null) {
            log.error("推送失败：找不到映射记录，ID: {}", mappingId);
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
            String targetLanguage = "English"; // 默认英语
            if ("TH".equalsIgnoreCase(mapping.getTargetCountry())) {
                targetLanguage = "Thai";
            } else if ("JP".equalsIgnoreCase(mapping.getTargetCountry())) {
                targetLanguage = "Japanese";
            }
            
            // 3. 执行翻译
            String translatedText = translationService.translate(sourceText, targetLanguage);
            log.info("翻译结果: {}", translatedText);
            
            // 4. 将翻译结果暂存到 remark 字段
            mapping.setRemark(translatedText);
            
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
            log.error("推送过程中发生未捕获异常", e);
            updateMappingStatus(mapping, 2L, "推送异常: " + e.getMessage());
        }
    }
    
    private void updateMappingStatus(CilsPlatformMapping mapping, Long status, String failReason) {
        mapping.setSyncStatus(status);
        mapping.setSyncFailReason(failReason);
        // 注意：这里调用 update 会把 mapping 对象里的 remark 也更新进去
        // 因为 mapping 对象在 pushProduct 方法里已经被 setRemark 了
        log.info("更新 Mapping 状态: status={}, remark={}", status, mapping.getRemark());
        mappingMapper.updateCilsPlatformMapping(mapping);
    }

    @Async
    public void syncStock(Long skuId) {
        log.info(">>> 开始同步库存到外部平台，SKU ID: {}", skuId);

        CilsPlatformMapping query = new CilsPlatformMapping();
        query.setSkuId(skuId);
        List<CilsPlatformMapping> mappings = mappingMapper.selectCilsPlatformMappingList(query);

        if (mappings.isEmpty()) {
            log.info("该 SKU 未关联任何平台，无需同步");
            return;
        }

        for (CilsPlatformMapping mapping : mappings) {
            if ("AMAZON".equalsIgnoreCase(mapping.getPlatformType())) {
                log.info("正在同步 Amazon 库存，Platform SKU: {}", mapping.getPlatformSku());
                AmazonPushUtils.syncStock(mapping.getPlatformSku(), 99L);
            }
        }

        log.info("<<< 库存同步任务完成");
    }
}
