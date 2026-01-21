package com.john.cils.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.*;

/**
 * 动态合规校验规则配置对象 cils_rule_config
 *
 * @author john
 * @date 2026-01-09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CilsRuleConfig extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 规则ID
     */
    private Long ruleId;

    /**
     * 规则名称
     */
    @Excel(name = "规则名称")
    private String ruleName;

    /**
     * 校验对象 (SPU, SKU)
     */
    @Excel(name = "校验对象")
    private String targetObject;

    /**
     * 校验字段 (productName, mainImage)
     */
    @Excel(name = "校验字段")
    private String targetField;

    /**
     * AI提示词
     */
    @Excel(name = "AI提示词")
    private String aiPrompt;

    /**
     * 校验失败提示信息
     */
    @Excel(name = "校验失败提示信息")
    private String errorMessage;

    /**
     * 启用状态
     */
    @Excel(name = "启用状态", readConverterExp = "1=启用,0=禁用")
    private Integer isActive;
}
