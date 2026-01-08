package com.john.cils.service;

import com.john.cils.domain.CilsProductSku;

import java.util.List;

/**
 * 商品单品规格表(SKU)Service接口
 *
 * @author john
 * @date 2024-05-20
 */
public interface ICilsProductSkuService {
    /**
     * 查询商品单品规格表(SKU)
     *
     * @param id 商品单品规格表(SKU)主键
     * @return 商品单品规格表(SKU)
     */
    public CilsProductSku selectCilsProductSkuById(Long id);

    /**
     * 查询商品单品规格表(SKU)列表
     *
     * @param cilsProductSku 商品单品规格表(SKU)
     * @return 商品单品规格表(SKU)集合
     */
    public List<CilsProductSku> selectCilsProductSkuList(CilsProductSku cilsProductSku);

    /**
     * 新增商品单品规格表(SKU)
     *
     * @param cilsProductSku 商品单品规格表(SKU)
     * @return 结果
     */
    public int insertCilsProductSku(CilsProductSku cilsProductSku);

    /**
     * 修改商品单品规格表(SKU)
     *
     * @param cilsProductSku 商品单品规格表(SKU)
     * @return 结果
     */
    public int updateCilsProductSku(CilsProductSku cilsProductSku);

    /**
     * 批量删除商品单品规格表(SKU)
     *
     * @param ids 需要删除的商品单品规格表(SKU)主键集合
     * @return 结果
     */
    public int deleteCilsProductSkuByIds(Long[] ids);

    /**
     * 删除商品单品规格表(SKU)信息
     *
     * @param id 商品单品规格表(SKU)主键
     * @return 结果
     */
    public int deleteCilsProductSkuById(Long id);

    /**
     * (高并发)扣减SKU库存
     *
     * @param skuId    要扣减的SKU ID
     * @param quantity 扣减数量
     * @return true-扣减成功, false-库存不足
     */
    public boolean deductStock(Long skuId, Integer quantity);
}
