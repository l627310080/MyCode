package com.john.cils.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Python合规校验规则配置对象 cils_python_rule
 * <p>
 * 该类映射数据库中的 cils_python_rule 表，用于存储校验规则的配置信息。
 *
 * @author ruoyi
 * @date 2026-01-06
 */
public class CilsPythonRule extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 合规规则友好名称
     */
    @Excel(name = "合规规则友好名称")
    private String ruleName;

    /**
     * Python脚本在服务器上的绝对物理路径
     */
    @Excel(name = "Python脚本在服务器上的绝对物理路径")
    private String scriptPath;

    /**
     * 校验类型：TITLE-标题审查, IMAGE-图像审查 (业务分类)
     */
    @Excel(name = "校验类型")
    private String ruleType;

    /**
     * 策略类型：PYTHON-脚本, REGEX-正则 (技术实现分类)
     * 用于 VerificationContext 决定使用哪种策略实现类
     */
    @Excel(name = "策略类型")
    private String strategyType;

    /**
     * 适用类目ID——对一个某个类目ID的配套的规则
     * NULL 或 0 表示全局规则，适用于所有商品。
     * 指定ID表示该规则仅适用于特定类目的商品。
     */
    @Excel(name = "适用类目ID")
    private Long categoryId;

    /**
     * 规则开关：1-启用，0-禁用
     */
    @Excel(name = "规则开关", readConverterExp = "1=启用,0=禁用")
    private Integer isActive;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getScriptPath() {
        return scriptPath;
    }

    public void setScriptPath(String scriptPath) {
        this.scriptPath = scriptPath;
    }

    public String getRuleType() {
        return ruleType;
    }

    public void setRuleType(String ruleType) {
        this.ruleType = ruleType;
    }

    public String getStrategyType() {
        return strategyType;
    }

    public void setStrategyType(String strategyType) {
        this.strategyType = strategyType;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getIsActive() {
        return isActive;
    }

    public void setIsActive(Integer isActive) {
        this.isActive = isActive;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("ruleName", getRuleName())
                .append("scriptPath", getScriptPath())
                .append("ruleType", getRuleType())
                .append("strategyType", getStrategyType())
                .append("categoryId", getCategoryId())
                .append("isActive", getIsActive())
                .append("createTime", getCreateTime())
                .toString();
    }
}
