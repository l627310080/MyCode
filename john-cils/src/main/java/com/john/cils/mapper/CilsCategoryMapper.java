package com.john.cils.mapper;

import com.john.cils.domain.CilsCategory;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 商品类目Mapper接口
 *
 * @author john
 * @date 2026-01-09
 */
@Mapper
public interface CilsCategoryMapper {
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
     * 删除商品类目
     *
     * @param categoryId 商品类目主键
     * @return 结果
     */
    public int deleteCilsCategoryByCategoryId(Long categoryId);

    /**
     * 批量删除商品类目
     *
     * @param categoryIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCilsCategoryByCategoryIds(Long[] categoryIds);
}
