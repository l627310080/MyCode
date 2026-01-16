package com.john.cils.verification.domain;

/**
 * 校验结果封装类
 * <p>
 * 用于在策略层 (Strategy) 和业务层 (Service) 之间传递校验结果。
 * 相比简单的 boolean 返回值，该类可以携带具体的失败原因 (message)，
 * 以便业务层能够将详细的错误信息记录到数据库或展示给用户。
 *
 * @author john
 * @date 2026-01-09
 */
public class VerificationResult {

    /**
     * 校验是否通过
     */
    private boolean success;

    /**
     * 校验信息 (成功提示或失败原因)
     */
    private String message;

    public VerificationResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    /**
     * 构建一个“校验通过”的结果对象
     */
    public static VerificationResult pass() {
        return new VerificationResult(true, "校验通过");
    }

    /**
     * 构建一个“校验失败”的结果对象
     *
     * @param message 失败的具体原因
     */
    public static VerificationResult fail(String message) {
        return new VerificationResult(false, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    // --- 兼容性别名方法 ---

    /**
     * 是否校验通过 (isSuccess 的别名)
     */
    public boolean isPass() {
        return success;
    }

    /**
     * 获取失败原因 (getMessage 的别名)
     */
    public String getFailReason() {
        return message;
    }
}
