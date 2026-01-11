package com.john.cils.service;

import com.john.cils.domain.CilsPlatformMapping;
import com.john.cils.mapper.CilsPlatformMappingMapper;
import com.john.cils.service.push.IPushStrategy;
import com.john.cils.utils.platform.AmazonPushUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 平台推送服务
 * <p>
 * 负责将本地商品信息推送到外部电商平台。
 * 使用策略模式分发不同平台的推送逻辑。
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
    public PlatformPushService(List<IPushStrategy> strategies) {
        strategies.forEach(strategy -> {
            strategyMap.put(strategy.getPlatformType(), strategy);
        });
    }

    /**
     * 推送商品到平台
     *
     * @param mappingId 映射记录ID
     */
    @Async
    public void pushProduct(Long mappingId) {
        CilsPlatformMapping mapping = mappingMapper.selectCilsPlatformMappingById(mappingId);
        if (mapping == null) {
            log.error("推送失败：找不到映射记录 {}", mappingId);
            return;
        }

        String platform = mapping.getPlatformType();
        log.info(">>> 开始推送商品到平台: {}", platform);

        IPushStrategy strategy = strategyMap.get(platform);
        if (strategy == null) {
            log.error("暂不支持平台: {}", platform);
            return;
        }

        boolean success = strategy.push(mapping);

        if (success) {
            log.info("<<< 推送成功！");
            mapping.setSyncStatus(1L);
            mappingMapper.updateCilsPlatformMapping(mapping);
        } else {
            log.error("<<< 推送失败！");
        }
    }

    /**
     * 同步库存到所有关联平台
     *
     * @param skuId 本地 SKU ID
     */
    @Async
    public void syncStock(Long skuId) {
        log.info(">>> 开始同步库存，SKU ID: {}", skuId);

        CilsPlatformMapping query = new CilsPlatformMapping();
        query.setSkuId(skuId);
        List<CilsPlatformMapping> mappings = mappingMapper.selectCilsPlatformMappingList(query);

        if (mappings.isEmpty()) {
            log.info("该 SKU 未关联任何平台，无需同步");
            return;
        }

        for (CilsPlatformMapping mapping : mappings) {
            // 这里也可以用策略模式，为了简单暂时只演示 Amazon
            if ("AMAZON".equalsIgnoreCase(mapping.getPlatformType())) {
                AmazonPushUtils.syncStock(mapping.getPlatformSku(), 99L);
            }
        }

        log.info("<<< 库存同步完成");
    }
}
