package com.john.cils.service;

import com.john.cils.domain.CilsCategory;
import com.ruoyi.common.core.domain.TreeSelect;

import java.util.List;

/**
 * 商品类目Service接口
 *
 * @author john
 * @date 2026-01-09
 */
public interface ICilsCategoryService {
    /**
     * 查询商品类目
     *
     * @param categoryId 商品类目主键
     * @return 商品类目
     */
    public CilsCategory selectCilsCategoryByCategoryId(Long categoryId);

    /**
     * 查询商品类目列表
     *
     * @param cilsCategory 商品类目
     * @return 商品类目集合
     */
    public List<CilsCategory> selectCilsCategoryList(CilsCategory cilsCategory);

    /**
     * 构建前端所需要树结构
     *
     * @param categorys 类目列表
     * @return 树结构列表
     */
    public List<CilsCategory> buildCategoryTree(List<CilsCategory> categorys);

    /**
     * 构建前端所需要下拉树结构
     *
     * @param categorys 类目列表
     * @return 下拉树结构列表
     */
    public List<TreeSelect> buildCategoryTreeSelect(List<CilsCategory> categorys);

    /**
     * 新增商品类目
     *
     * @param cilsCategory 商品类目
     * @return 结果
     */
    public int insertCilsCategory(CilsCategory cilsCategory);

    /**
     * 修改商品类目
     *
     * @param cilsCategory 商品类目
     * @return 结果
     */
    public int updateCilsCategory(CilsCategory cilsCategory);

    /**
     * 批量删除商品类目
     *
     * @param categoryIds 需要删除的商品类目主键集合
     * @return 结果
     */
    public int deleteCilsCategoryByCategoryIds(Long[] categoryIds);

    /**
     * 删除商品类目信息
     *
     * @param categoryId 商品类目主键
     * @return 结果
     */
    public int deleteCilsCategoryByCategoryId(Long categoryId);
}
