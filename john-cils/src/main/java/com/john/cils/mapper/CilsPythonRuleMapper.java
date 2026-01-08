package com.john.cils.mapper;

import com.john.cils.domain.CilsPythonRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Python合规校验规则配置Mapper接口
 *
 * @author ruoyi
 * @date 2026-01-06
 */
@Mapper
public interface CilsPythonRuleMapper {
    /**
     * 查询Python合规校验规则配置
     *
     * @param id Python合规校验规则配置主键
     * @return Python合规校验规则配置
     */
    public CilsPythonRule selectCilsPythonRuleById(Long id);

    /**
     * 查询Python合规校验规则配置列表 (后台管理用)
     *
     * @param cilsPythonRule Python合规校验规则配置
     * @return Python合规校验规则配置集合
     */
    public List<CilsPythonRule> selectCilsPythonRuleList(CilsPythonRule cilsPythonRule);

    /**
     * 根据类目ID查询适用的规则 (校验引擎用)
     * <p>
     * 逻辑：查询所有全局规则 (categoryId 为空或0) + 指定类目的规则
     *
     * @param categoryId 商品所属的类目ID
     * @return 适用的规则列表
     */
    public List<CilsPythonRule> selectRulesForSpu(@Param("categoryId") Long categoryId);

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
     * 删除Python合规校验规则配置
     *
     * @param id Python合规校验规则配置主键
     * @return 结果
     */
    public int deleteCilsPythonRuleById(Long id);

    /**
     * 批量删除Python合规校验规则配置
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCilsPythonRuleByIds(Long[] ids);
}
