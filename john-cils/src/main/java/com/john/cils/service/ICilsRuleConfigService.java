package com.john.cils.service;

import com.john.cils.domain.CilsRuleConfig;

import java.util.List;

/**
 * 动态合规校验规则配置Service接口
 *
 * @author john
 * @date 2026-01-06
 */
public interface ICilsRuleConfigService {
    /**
     * 查询动态合规校验规则配置
     *
     * @param ruleId 动态合规校验规则配置主键
     * @return 动态合规校验规则配置
     */
    public CilsRuleConfig selectCilsRuleConfigById(Long ruleId);

    /**
     * 查询动态合规校验规则配置列表
     *
     * @param cilsRuleConfig 动态合规校验规则配置
     * @return 动态合规校验规则配置集合
     */
    public List<CilsRuleConfig> selectCilsRuleConfigList(CilsRuleConfig cilsRuleConfig);

    /**
     * 新增动态合规校验规则配置
     *
     * @param cilsRuleConfig 动态合规校验规则配置
     * @return 结果
     */
    public int insertCilsRuleConfig(CilsRuleConfig cilsRuleConfig);

    /**
     * 修改动态合规校验规则配置
     *
     * @param cilsRuleConfig 动态合规校验规则配置
     * @return 结果
     */
    public int updateCilsRuleConfig(CilsRuleConfig cilsRuleConfig);

    /**
     * 批量删除动态合规校验规则配置
     *
     * @param ruleIds 需要删除的动态合规校验规则配置主键集合
     * @return 结果
     */
    public int deleteCilsRuleConfigByIds(Long[] ruleIds);

    /**
     * 删除动态合规校验规则配置信息
     *
     * @param ruleId 动态合规校验规则配置主键
     * @return 结果
     */
    public int deleteCilsRuleConfigById(Long ruleId);
}
