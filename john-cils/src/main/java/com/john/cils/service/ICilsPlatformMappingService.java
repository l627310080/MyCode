package com.john.cils.service;

import com.john.cils.domain.CilsPlatformMapping;

import java.util.List;

/**
 * 跨平台商品映射Service接口
 *
 * @author ruoyi
 * @date 2026-01-06
 */
public interface ICilsPlatformMappingService {
    /**
     * 查询跨平台商品映射
     *
     * @param id 跨平台商品映射主键
     * @return 跨平台商品映射
     */
    public CilsPlatformMapping selectCilsPlatformMappingById(Long id);

    /**
     * 查询跨平台商品映射列表
     *
     * @param cilsPlatformMapping 跨平台商品映射
     * @return 跨平台商品映射集合
     */
    public List<CilsPlatformMapping> selectCilsPlatformMappingList(CilsPlatformMapping cilsPlatformMapping);

    /**
     * 新增跨平台商品映射
     *
     * @param cilsPlatformMapping 跨平台商品映射
     * @return 结果
     */
    public int insertCilsPlatformMapping(CilsPlatformMapping cilsPlatformMapping);

    /**
     * 修改跨平台商品映射
     *
     * @param cilsPlatformMapping 跨平台商品映射
     * @return 结果
     */
    public int updateCilsPlatformMapping(CilsPlatformMapping cilsPlatformMapping);

    /**
     * 批量删除跨平台商品映射
     *
     * @param ids 需要删除的跨平台商品映射主键集合
     * @return 结果
     */
    public int deleteCilsPlatformMappingByIds(Long[] ids);

    /**
     * 删除跨平台商品映射信息
     *
     * @param id 跨平台商品映射主键
     * @return 结果
     */
    public int deleteCilsPlatformMappingById(Long id);
}
