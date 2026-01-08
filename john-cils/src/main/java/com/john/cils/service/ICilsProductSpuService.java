package com.john.cils.service;

import com.john.cils.domain.CilsProductSpu;

import java.util.List;

public interface ICilsProductSpuService {
    /**
     * 查询跨境商品标准信息(SPU)
     *
     * @param id 跨境商品标准信息(SPU)主键
     * @return 跨境商品标准信息(SPU)
     */
    public CilsProductSpu selectCilsProductSpuById(Long id);

    /**
     * 查询跨境商品标准信息(SPU)列表
     *
     * @param cilsProductSpu 跨境商品标准信息(SPU)
     * @return 跨境商品标准信息(SPU)集合
     */
    public List<CilsProductSpu> selectCilsProductSpuList(CilsProductSpu cilsProductSpu);

    /**
     * 新增跨境商品标准信息(SPU)
     *
     * @param cilsProductSpu 跨境商品标准信息(SPU)
     * @return 结果
     */
    public int insertCilsProductSpu(CilsProductSpu cilsProductSpu);

    /**
     * 修改跨境商品标准信息(SPU)
     *
     * @param cilsProductSpu 跨境商品标准信息(SPU)
     * @return 结果
     */
    public int updateCilsProductSpu(CilsProductSpu cilsProductSpu);

    /**
     * 批量删除跨境商品标准信息(SPU)
     *
     * @param ids 需要删除的跨境商品标准信息(SPU)主键集合
     * @return 结果
     */
    public int deleteCilsProductSpuByIds(Long[] ids);

    /**
     * 删除跨境商品标准信息(SPU)信息
     *
     * @param id 跨境商品标准信息(SPU)主键
     * @return 结果
     */
    public int deleteCilsProductSpuById(Long id);
}
