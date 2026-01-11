package com.john.cils.verification.domain;

/**
 * 可校验对象接口
 * <p>
 * 所有需要进行合规校验的实体（如 SPU、SKU）都必须实现此接口。
 * 这使得校验服务 (AsyncVerificationService) 可以统一处理不同类型的对象，
 * 而无需为每个对象编写重复的校验逻辑。
 *
 * @author john
 * @date 2026-01-06
 */
public interface Verifiable {

    /**
     * 获取对象的唯一标识 ID
     */
    Long getId();

    /**
     * 获取对象的名称或编码 (用于日志记录)
     */
    String getIdentity();

    /**
     * 获取对象所属的类目 ID (用于匹配类目特定规则)
     * 如果对象本身没有类目 (如 SKU)，则应返回其父级 (SPU) 的类目 ID。
     */
    Long getCategoryId();

    /**
     * 更新审核状态
     *
     * @param status 状态码
     * @param remark 备注信息
     */
    void updateAuditStatus(Integer status, String remark);
}
