package com.john.cils.mapper;

import com.john.cils.domain.CilsRuleConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 动态合规校验规则配置Mapper接口
 *
 * @author john
 * @date 2026-01-09
 */
@Mapper
public interface CilsRuleConfigMapper {
    /**
     * 查询规则配置
     *
     * @param ruleId 规则ID
     * @return 规则配置
     */
    public CilsRuleConfig selectCilsRuleConfigById(Long ruleId);

    /**
     * 查询规则配置列表
     *
     * @param cilsRuleConfig 规则配置
     * @return 规则配置集合
     */
    public List<CilsRuleConfig> selectCilsRuleConfigList(CilsRuleConfig cilsRuleConfig);

    /**
     * 根据目标对象查询适用的规则 (校验引擎用)
     *
     * @param targetObject 校验对象 (SPU, SKU)
     * @return 适用的规则列表
     */
    public List<CilsRuleConfig> selectRulesByTarget(@Param("targetObject") String targetObject);

    /**
     * 根据目标对象和字段查询特定规则（用于获取模板）
     *
     * @param targetObject TEMPLATE
     * @param targetField  checker_image / checker_text
     * @return
     */
    public CilsRuleConfig selectRuleByTargetAndField(@Param("targetObject") String targetObject, @Param("targetField") String targetField);

    /**
     * 新增规则配置
     *
     * @param cilsRuleConfig 规则配置
     * @return 结果
     */
    public int insertCilsRuleConfig(CilsRuleConfig cilsRuleConfig);

    /**
     * 修改规则配置
     *
     * @param cilsRuleConfig 规则配置
     * @return 结果
     */
    public int updateCilsRuleConfig(CilsRuleConfig cilsRuleConfig);

    /**
     * 删除规则配置
     *
     * @param ruleId 规则ID
     * @return 结果
     */
    public int deleteCilsRuleConfigById(Long ruleId);

    /**
     * 批量删除规则配置
     *
     * @param ruleIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCilsRuleConfigByIds(Long[] ruleIds);
}
