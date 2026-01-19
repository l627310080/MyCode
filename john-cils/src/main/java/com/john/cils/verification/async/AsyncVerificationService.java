package com.john.cils.verification.async;

import com.john.cils.common.constant.CilsConstants;
import com.john.cils.domain.CilsPlatformMapping;
import com.john.cils.domain.CilsProductSku;
import com.john.cils.domain.CilsProductSpu;
import com.john.cils.domain.CilsRuleConfig;
import com.john.cils.mapper.CilsPlatformMappingMapper;
import com.john.cils.mapper.CilsProductSkuMapper;
import com.john.cils.mapper.CilsProductSpuMapper;
import com.john.cils.mapper.CilsRuleConfigMapper;
import com.john.cils.service.PlatformPushService;
import com.john.cils.verification.domain.Verifiable;
import com.john.cils.verification.domain.VerificationResult;
import com.john.cils.verification.engine.VerificationEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 异步校验服务
 * <p>
 * 负责调度 AI 校验任务，支持异步和同步两种模式。
 * 它是连接业务层 (Service) 和校验引擎 (Engine) 的桥梁。
 *
 * @author john
 * @date 2026-01-09
 */
@Service
public class AsyncVerificationService {

    private static final Logger log = LoggerFactory.getLogger(AsyncVerificationService.class);

    // 校验引擎，负责执行具体的规则校验逻辑
    @Autowired
    private VerificationEngine verificationEngine;

    // 规则配置 Mapper，用于查询校验规则
    @Autowired
    private CilsRuleConfigMapper ruleConfigMapper;

    // SPU Mapper，用于更新 SPU 状态
    @Autowired
    private CilsProductSpuMapper spuMapper;

    // SKU Mapper，用于更新 SKU 状态
    @Autowired
    private CilsProductSkuMapper skuMapper;

    // Mapping Mapper，用于查询待推送的映射记录
    @Autowired
    private CilsPlatformMappingMapper mappingMapper;

    // 推送服务，用于将校验通过的商品推送到外部平台
    @Autowired
    private PlatformPushService platformPushService;

    /**
     * 触发异步校验任务
     * <p>
     * 该方法被 @Async 注解修饰，会在独立的线程池中执行，不会阻塞主线程。
     * 适用于 SPU 新增/修改，以及 SKU 的非阻塞校验场景。
     *
     * @param data 待校验的数据对象 (SPU 或 SKU)
     */
    @Async
    public void triggerVerification(Verifiable data) {
        log.info(">>> 异步校验任务开始，对象: {}", data.getIdentity());

        try {
            // 执行具体的校验逻辑
            VerificationResult result = doVerify(data);

            // 根据校验结果计算新的状态
            // 1 = 审核通过, 2 = 合规拦截
            int status = result.isPass() ? 1 : 2;
            // 如果通过，备注为“校验通过”；如果不通过，备注为具体的失败原因
            String remark = result.isPass() ? "校验通过" : result.getFailReason();

            // 更新内存中对象的审核状态和备注
            data.updateAuditStatus(status, remark);

            // 将更新后的状态回写到数据库
            if (data instanceof CilsProductSpu) {
                spuMapper.updateCilsProductSpu((CilsProductSpu) data);
            } else if (data instanceof CilsProductSku) {
                CilsProductSku sku = (CilsProductSku) data;
                skuMapper.updateCilsProductSku(sku);

                // 关键逻辑：如果 SKU 校验通过，自动触发关联 Mapping 的推送
                if (result.isPass()) {
                    triggerPushForSku(sku.getId());
                }
            }

            log.info("<<< 异步校验任务结束，最终状态: {}", status);

        } catch (Exception e) {
            log.error("异步校验过程中发生异常", e);
        }
    }

    /**
     * 触发指定 SKU 关联且处于“等待校验”状态的 Mapping 推送任务
     * 
     * @param skuId SKU ID
     */
    private void triggerPushForSku(Long skuId) {
        CilsPlatformMapping query = new CilsPlatformMapping();
        query.setSkuId(skuId);
        // 查询该 SKU 下所有的平台映射
        List<CilsPlatformMapping> mappings = mappingMapper.selectCilsPlatformMappingList(query);

        if (mappings != null && !mappings.isEmpty()) {
            log.info("SKU {} 校验通过，开始扫描待释放的同步记录", skuId);
            boolean hasReleased = false;
            for (CilsPlatformMapping mapping : mappings) {
                // 只有处于“等待校验”状态的记录才允许被释放并推送
                if (CilsConstants.SYNC_STATUS_WAIT_VERIFY.equals(mapping.getSyncStatus())) {
                    log.info("释放 Mapping ID: {}, 状态由 WAIT_VERIFY 转为 待同步", mapping.getId());
                    mapping.setSyncStatus(0L); // 0 = 待同步 (已就绪)
                    mappingMapper.updateCilsPlatformMapping(mapping);

                    // 1. 发起全量铺货 (异步逻辑由 PlatformPushService 内部实现)
                    platformPushService.pushProduct(mapping.getId());
                    hasReleased = true;
                }
            }

            // 2. 如果发生了记录释放，则触发一次该 SKU 的全局库存同步
            if (hasReleased) {
                log.info("SKU {} 存在新释放的同步记录，触发精准库存同步", skuId);
                platformPushService.syncStock(skuId);
            }
        }
    }

    /**
     * 校验逻辑的核心实现
     * 
     * @param data 待校验的数据对象
     * @return 校验结果
     */
    private VerificationResult doVerify(Verifiable data) {
        // 1. 确定校验对象的类型 (SPU 或 SKU)
        String targetObject = "";
        if (data instanceof CilsProductSpu) {
            targetObject = "SPU";
        } else if (data instanceof CilsProductSku) {
            targetObject = "SKU";
        }

        // 2. 从数据库中查询针对该类型的校验规则
        List<CilsRuleConfig> rules = ruleConfigMapper.selectRulesByTarget(targetObject);

        // 如果没有配置任何规则，默认视为通过
        if (rules == null || rules.isEmpty()) {
            return VerificationResult.pass();
        }

        // 3. 调用校验引擎执行规则检查
        return verificationEngine.executeBatch(data, rules);
    }
}
