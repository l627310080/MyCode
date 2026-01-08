package com.john.cils.mapper;

import com.john.cils.domain.CilsProductSku;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 商品单品规格(SKU)Mapper接口
 *
 * @author ruoyi
 * @date 2026-01-06
 */
@Mapper
public interface CilsProductSkuMapper {
    /**
     * 查询商品单品规格(SKU)
     *
     * @param id 商品单品规格(SKU)主键
     * @return 商品单品规格(SKU)
     */
    public CilsProductSku selectCilsProductSkuById(Long id);

    /**
     * 查询商品单品规格(SKU)列表
     *
     * @param cilsProductSku 商品单品规格(SKU)
     * @return 商品单品规格(SKU)集合
     */
    public List<CilsProductSku> selectCilsProductSkuList(CilsProductSku cilsProductSku);

    /**
     * 新增商品单品规格(SKU)
     *
     * @param cilsProductSku 商品单品规格(SKU)
     * @return 结果
     */
    public int insertCilsProductSku(CilsProductSku cilsProductSku);

    /**
     * 修改商品单品规格(SKU)
     *
     * @param cilsProductSku 商品单品规格(SKU)
     * @return 结果
     */
    public int updateCilsProductSku(CilsProductSku cilsProductSku);

    /**
     * 删除商品单品规格(SKU)
     *
     * @param id 商品单品规格(SKU)主键
     * @return 结果
     */
    public int deleteCilsProductSkuById(Long id);

    /**
     * 批量删除商品单品规格(SKU)
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCilsProductSkuByIds(Long[] ids);
}
