package com.john.cils.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 动态合规校验规则配置对象 cils_rule_config
 *
 * @author john
 * @date 2026-01-09
 */
public class CilsRuleConfig extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 规则ID */
    private Long ruleId;

    /** 规则名称 */
    @Excel(name = "规则名称")
    private String ruleName;

    /** 校验对象 (SPU, SKU) */
    @Excel(name = "校验对象")
    private String targetObject;

    /** 校验字段 (productName, mainImage) */
    @Excel(name = "校验字段")
    private String targetField;

    /** AI提示词 */
    @Excel(name = "AI提示词")
    private String aiPrompt;

    /** 校验失败提示信息 */
    @Excel(name = "校验失败提示信息")
    private String errorMessage;

    /** 启用状态 */
    @Excel(name = "启用状态", readConverterExp = "1=启用,0=禁用")
    private Integer isActive;

    public Long getRuleId() { return ruleId; }
    public void setRuleId(Long ruleId) { this.ruleId = ruleId; }

    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }

    public String getTargetObject() { return targetObject; }
    public void setTargetObject(String targetObject) { this.targetObject = targetObject; }

    public String getTargetField() { return targetField; }
    public void setTargetField(String targetField) { this.targetField = targetField; }

    public String getAiPrompt() { return aiPrompt; }
    public void setAiPrompt(String aiPrompt) { this.aiPrompt = aiPrompt; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Integer getIsActive() { return isActive; }
    public void setIsActive(Integer isActive) { this.isActive = isActive; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("ruleId", getRuleId())
                .append("ruleName", getRuleName())
                .append("targetObject", getTargetObject())
                .append("targetField", getTargetField())
                .append("aiPrompt", getAiPrompt())
                .append("errorMessage", getErrorMessage())
                .append("isActive", getIsActive())
                .toString();
    }
}
