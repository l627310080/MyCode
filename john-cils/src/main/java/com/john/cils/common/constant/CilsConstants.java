package com.john.cils.common.constant;

/**
 * CILS 跨境系统通用常量信息
 *
 * @author john
 * @date 2026-01-06
 */
public class CilsConstants {
    /**
     * 校验策略类型：Python脚本
     */
    public static final String STRATEGY_TYPE_PYTHON = "PYTHON";

    /**
     * 校验策略类型：正则表达式
     */
    public static final String STRATEGY_TYPE_REGEX = "REGEX";

    /**
     * 业务规则类型：标题检查
     */
    public static final String RULE_TYPE_TITLE = "TITLE";

    /**
     * 业务规则类型：图片检查
     */
    public static final String RULE_TYPE_IMAGE = "IMAGE";

    /**
     * 审核状态：待审核
     */
    public static final Integer AUDIT_STATUS_WAITING = 0;

    /**
     * 审核状态：审核通过
     */
    public static final Integer AUDIT_STATUS_PASS = 1;

    /**
     * 审核状态：合规拦截
     */
    public static final Integer AUDIT_STATUS_BLOCK = 2;

    /**
     * 同步状态：已同步
     */
    public static final Long SYNC_STATUS_DONE = 1L;

    /**
     * 同步状态：同步失败
     */
    public static final Long SYNC_STATUS_FAIL = 2L;

    /**
     * 同步状态：等待校验 (AI 校验通过前不可同步)
     */
    public static final Long SYNC_STATUS_WAIT_VERIFY = 3L;

    /**
     * 系统命令：Python解释器名称
     */
    public static final String CMD_PYTHON = "python";

    /**
     * 脚本输出协议：通过
     */
    public static final String SCRIPT_OUT_PASS = "PASS";

    /**
     * 脚本输出协议：拦截
     */
    public static final String SCRIPT_OUT_BLOCK = "BLOCK";

    /**
     * 定价模式：固定价格
     */
    public static final String PRICE_MODE_FIXED = "FIXED";

    /**
     * 定价模式：倍数定价
     */
    public static final String PRICE_MODE_MULTIPLIER = "MULTIPLIER";
}
