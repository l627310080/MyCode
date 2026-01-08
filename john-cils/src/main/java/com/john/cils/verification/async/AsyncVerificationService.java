package com.john.cils.verification.async;

import com.john.cils.common.constant.CilsConstants;
import com.john.cils.domain.CilsProductSpu;
import com.john.cils.domain.CilsPythonRule;
import com.john.cils.mapper.CilsProductSpuMapper;
import com.john.cils.mapper.CilsPythonRuleMapper;
import com.john.cils.verification.context.VerificationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 异步合规校验服务
 * <p>
 * 该服务负责协调校验流程，包括：
 * 1. 获取适用的校验规则
 * 2. 遍历规则执行校验
 * 3. 更新最终状态
 * <p>
 * 使用 @Async 注解确保整个流程在独立线程中执行，不阻塞主业务。
 *
 * @author john
 * @date 2026-01-06
 */
@Service
public class AsyncVerificationService {

    private static final Logger log = LoggerFactory.getLogger(AsyncVerificationService.class);

    @Autowired
    private VerificationContext verificationContext;

    @Autowired
    private CilsPythonRuleMapper ruleMapper;

    @Autowired
    private CilsProductSpuMapper spuMapper;

    /**
     * 触发异步校验任务
     *
     * @param spu 待校验的商品ID
     */
    @Async
    public void triggerVerification(CilsProductSpu spu) {
        log.info(">>> 异步校验任务开始，SPU ID: {}", spu.getId());

        try {
            // 1. 获取适用的校验规则
            // 逻辑：查询所有全局规则 + 当前商品所属类目的特定规则
            // 这里正确调用了 selectRulesForSpu 方法
            List<CilsPythonRule> rules = ruleMapper.selectRulesForSpu(spu.getCategoryId());

            if (rules.isEmpty()) {
                log.info("没有适用的校验规则，默认通过");
                updateAuditStatus(spu, CilsConstants.AUDIT_STATUS_PASS, "无校验规则，自动通过");
                return;
            }

            // 2. 遍历规则执行校验
            boolean allPass = true;
            String failReason = "";

            for (CilsPythonRule rule : rules) {
                try {
                    // 委托 Context 执行策略
                    boolean pass = verificationContext.executeStrategy(spu, rule);

                    if (!pass) {
                        log.warn("规则校验未通过: {}, 规则名: {}", spu.getSpuCode(), rule.getRuleName());
                        allPass = false;
                        failReason = "规则拦截: " + rule.getRuleName();
                        break; // 快速失败机制
                    }
                } catch (Exception e) {
                    log.error("规则执行异常: " + rule.getRuleName(), e);
                    allPass = false;
                    failReason = "系统异常: " + rule.getRuleName();
                    break;
                }
            }

            // 3. 更新最终状态
            int finalStatus = allPass ? CilsConstants.AUDIT_STATUS_PASS : CilsConstants.AUDIT_STATUS_BLOCK;
            String remark = allPass ? "校验通过" : failReason;

            updateAuditStatus(spu, finalStatus, remark);

            log.info("<<< 异步校验任务结束，最终状态: {}", finalStatus);

        } catch (Exception e) {
            log.error("异步校验服务发生未知错误", e);
        }
    }

    /**
     * 更新商品审核状态及备注
     */
    private void updateAuditStatus(CilsProductSpu spu, int status, String remark) {
        spu.setIsAudit(status);
        spu.setRemark(remark);
        spuMapper.updateCilsProductSpu(spu);
    }
}
