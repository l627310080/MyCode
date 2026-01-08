package com.john.cils.service.impl;

import com.john.cils.domain.CilsPythonRule;
import com.john.cils.mapper.CilsPythonRuleMapper;
import com.john.cils.service.ICilsPythonRuleService;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Python合规校验规则配置Service业务层处理
 *
 * @author ruoyi
 * @date 2026-01-06
 */
@Service
public class CilsPythonRuleServiceImpl implements ICilsPythonRuleService {
    @Autowired
    private CilsPythonRuleMapper cilsPythonRuleMapper;

    /**
     * 查询Python合规校验规则配置
     *
     * @param id Python合规校验规则配置主键
     * @return Python合规校验规则配置
     */
    @Override
    public CilsPythonRule selectCilsPythonRuleById(Long id) {
        return cilsPythonRuleMapper.selectCilsPythonRuleById(id);
    }

    /**
     * 查询Python合规校验规则配置列表
     *
     * @param cilsPythonRule Python合规校验规则配置
     * @return Python合规校验规则配置
     */
    @Override
    public List<CilsPythonRule> selectCilsPythonRuleList(CilsPythonRule cilsPythonRule) {
        return cilsPythonRuleMapper.selectCilsPythonRuleList(cilsPythonRule);
    }

    /**
     * 新增Python合规校验规则配置
     *
     * @param cilsPythonRule Python合规校验规则配置
     * @return 结果
     */
    @Override
    public int insertCilsPythonRule(CilsPythonRule cilsPythonRule) {
        cilsPythonRule.setCreateTime(DateUtils.getNowDate());
        return cilsPythonRuleMapper.insertCilsPythonRule(cilsPythonRule);
    }

    /**
     * 修改Python合规校验规则配置
     *
     * @param cilsPythonRule Python合规校验规则配置
     * @return 结果
     */
    @Override
    public int updateCilsPythonRule(CilsPythonRule cilsPythonRule) {
        return cilsPythonRuleMapper.updateCilsPythonRule(cilsPythonRule);
    }

    /**
     * 批量删除Python合规校验规则配置
     *
     * @param ids 需要删除的Python合规校验规则配置主键
     * @return 结果
     */
    @Override
    public int deleteCilsPythonRuleByIds(Long[] ids) {
        return cilsPythonRuleMapper.deleteCilsPythonRuleByIds(ids);
    }

    /**
     * 删除Python合规校验规则配置信息
     *
     * @param id Python合规校验规则配置主键
     * @return 结果
     */
    @Override
    public int deleteCilsPythonRuleById(Long id) {
        return cilsPythonRuleMapper.deleteCilsPythonRuleById(id);
    }
}
