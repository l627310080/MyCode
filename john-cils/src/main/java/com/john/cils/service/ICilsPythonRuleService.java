package com.john.cils.service;

import com.john.cils.domain.CilsPythonRule;

import java.util.List;

/**
 * Python合规校验规则配置Service接口
 *
 * @author ruoyi
 * @date 2026-01-06
 */
public interface ICilsPythonRuleService {
    /**
     * 查询Python合规校验规则配置
     *
     * @param id Python合规校验规则配置主键
     * @return Python合规校验规则配置
     */
    public CilsPythonRule selectCilsPythonRuleById(Long id);

    /**
     * 查询Python合规校验规则配置列表
     *
     * @param cilsPythonRule Python合规校验规则配置
     * @return Python合规校验规则配置集合
     */
    public List<CilsPythonRule> selectCilsPythonRuleList(CilsPythonRule cilsPythonRule);

    /**
     * 新增Python合规校验规则配置
     *
     * @param cilsPythonRule Python合规校验规则配置
     * @return 结果
     */
    public int insertCilsPythonRule(CilsPythonRule cilsPythonRule);

    /**
     * 修改Python合规校验规则配置
     *
     * @param cilsPythonRule Python合规校验规则配置
     * @return 结果
     */
    public int updateCilsPythonRule(CilsPythonRule cilsPythonRule);

    /**
     * 批量删除Python合规校验规则配置
     *
     * @param ids 需要删除的Python合规校验规则配置主键集合
     * @return 结果
     */
    public int deleteCilsPythonRuleByIds(Long[] ids);

    /**
     * 删除Python合规校验规则配置信息
     *
     * @param id Python合规校验规则配置主键
     * @return 结果
     */
    public int deleteCilsPythonRuleById(Long id);
}
