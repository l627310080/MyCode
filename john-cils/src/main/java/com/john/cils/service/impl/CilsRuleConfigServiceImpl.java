package com.john.cils.service.impl;

import com.john.cils.domain.CilsRuleConfig;
import com.john.cils.mapper.CilsRuleConfigMapper;
import com.john.cils.service.ICilsRuleConfigService;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 动态合规校验规则配置Service业务层处理
 *
 * @author john
 * @date 2026-01-09
 */
@Service
public class CilsRuleConfigServiceImpl implements ICilsRuleConfigService {
    @Autowired
    private CilsRuleConfigMapper cilsRuleConfigMapper;

    /**
     * 查询动态合规校验规则配置
     *
     * @param ruleId 动态合规校验规则配置主键
     * @return 动态合规校验规则配置
     */
    @Override
    public CilsRuleConfig selectCilsRuleConfigById(Long ruleId) {
        return cilsRuleConfigMapper.selectCilsRuleConfigById(ruleId);
    }

    /**
     * 查询动态合规校验规则配置列表
     *
     * @param cilsRuleConfig 动态合规校验规则配置
     * @return 动态合规校验规则配置
     */
    @Override
    public List<CilsRuleConfig> selectCilsRuleConfigList(CilsRuleConfig cilsRuleConfig) {
        return cilsRuleConfigMapper.selectCilsRuleConfigList(cilsRuleConfig);
    }

    /**
     * 新增动态合规校验规则配置
     *
     * @param cilsRuleConfig 动态合规校验规则配置
     * @return 结果
     */
    @Override
    public int insertCilsRuleConfig(CilsRuleConfig cilsRuleConfig) {
        cilsRuleConfig.setCreateTime(DateUtils.getNowDate());
        return cilsRuleConfigMapper.insertCilsRuleConfig(cilsRuleConfig);
    }

    /**
     * 修改动态合规校验规则配置
     *
     * @param cilsRuleConfig 动态合规校验规则配置
     * @return 结果
     */
    @Override
    public int updateCilsRuleConfig(CilsRuleConfig cilsRuleConfig) {
        cilsRuleConfig.setUpdateTime(DateUtils.getNowDate());
        return cilsRuleConfigMapper.updateCilsRuleConfig(cilsRuleConfig);
    }

    /**
     * 批量删除动态合规校验规则配置
     *
     * @param ruleIds 需要删除的动态合规校验规则配置主键
     * @return 结果
     */
    @Override
    public int deleteCilsRuleConfigByIds(Long[] ruleIds) {
        return cilsRuleConfigMapper.deleteCilsRuleConfigByIds(ruleIds);
    }

    /**
     * 删除动态合规校验规则配置信息
     *
     * @param ruleId 动态合规校验规则配置主键
     * @return 结果
     */
    @Override
    public int deleteCilsRuleConfigById(Long ruleId) {
        return cilsRuleConfigMapper.deleteCilsRuleConfigById(ruleId);
    }
}
