package com.john.cils.verification.async;

import com.john.cils.common.constant.CilsConstants;
import com.john.cils.domain.CilsProductSku;
import com.john.cils.domain.CilsProductSpu;
import com.john.cils.domain.CilsRuleConfig;
import com.john.cils.mapper.CilsProductSkuMapper;
import com.john.cils.mapper.CilsProductSpuMapper;
import com.john.cils.mapper.CilsRuleConfigMapper;
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
 * 异步合规校验服务
 *
 * @author john
 * @date 2026-01-09
 */
@Service
public class AsyncVerificationService {

    private static final Logger log = LoggerFactory.getLogger(AsyncVerificationService.class);

    @Autowired
    private VerificationEngine verificationEngine;

    @Autowired
    private CilsRuleConfigMapper ruleConfigMapper;

    @Autowired
    private CilsProductSpuMapper spuMapper;

    @Autowired
    private CilsProductSkuMapper skuMapper;

    @Async
    public void triggerVerification(Verifiable target) {
        log.info(">>> 异步校验任务开始，对象: {}", target.getIdentity());

        try {
            String targetObject = (target instanceof CilsProductSpu) ? "SPU" : "SKU";
            List<CilsRuleConfig> rules = ruleConfigMapper.selectRulesByTarget(targetObject);

            if (rules.isEmpty()) {
                log.info("没有适用的校验规则，默认通过");
                updateStatus(target, CilsConstants.AUDIT_STATUS_PASS, "无校验规则，自动通过");
                return;
            }

            // 批量执行校验
            VerificationResult result = verificationEngine.executeBatch(target, rules);

            int finalStatus = result.isSuccess() ? CilsConstants.AUDIT_STATUS_PASS : CilsConstants.AUDIT_STATUS_BLOCK;
            String remark = result.isSuccess() ? "校验通过" : result.getMessage();

            updateStatus(target, finalStatus, remark);

            log.info("<<< 异步校验任务结束，最终状态: {}", finalStatus);

        } catch (Exception e) {
            log.error("异步校验服务发生未知错误", e);
        }
    }

    private void updateStatus(Verifiable target, int status, String remark) {
        target.updateAuditStatus(status, remark);

        if (target instanceof CilsProductSpu) {
            spuMapper.updateCilsProductSpu((CilsProductSpu) target);
        } else if (target instanceof CilsProductSku) {
            skuMapper.updateCilsProductSku((CilsProductSku) target);
        }
    }
}
