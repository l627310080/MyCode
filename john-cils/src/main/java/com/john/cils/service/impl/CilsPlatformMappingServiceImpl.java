package com.john.cils.service.impl;

import com.john.cils.domain.CilsPlatformMapping;
import com.john.cils.mapper.CilsPlatformMappingMapper;
import com.john.cils.service.ICilsPlatformMappingService;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 跨平台商品映射Service业务层处理
 *
 * @author ruoyi
 * @date 2026-01-06
 */
@Service
public class CilsPlatformMappingServiceImpl implements ICilsPlatformMappingService {
    @Autowired
    private CilsPlatformMappingMapper cilsPlatformMappingMapper;

    /**
     * 查询跨平台商品映射
     *
     * @param id 跨平台商品映射主键
     * @return 跨平台商品映射
     */
    @Override
    public CilsPlatformMapping selectCilsPlatformMappingById(Long id) {
        return cilsPlatformMappingMapper.selectCilsPlatformMappingById(id);
    }

    /**
     * 查询跨平台商品映射列表
     *
     * @param cilsPlatformMapping 跨平台商品映射
     * @return 跨平台商品映射
     */
    @Override
    public List<CilsPlatformMapping> selectCilsPlatformMappingList(CilsPlatformMapping cilsPlatformMapping) {
        return cilsPlatformMappingMapper.selectCilsPlatformMappingList(cilsPlatformMapping);
    }

    /**
     * 新增跨平台商品映射
     *
     * @param cilsPlatformMapping 跨平台商品映射
     * @return 结果
     */
    @Override
    public int insertCilsPlatformMapping(CilsPlatformMapping cilsPlatformMapping) {
        cilsPlatformMapping.setCreateTime(DateUtils.getNowDate());
        return cilsPlatformMappingMapper.insertCilsPlatformMapping(cilsPlatformMapping);
    }

    /**
     * 修改跨平台商品映射
     *
     * @param cilsPlatformMapping 跨平台商品映射
     * @return 结果
     */
    @Override
    public int updateCilsPlatformMapping(CilsPlatformMapping cilsPlatformMapping) {
        return cilsPlatformMappingMapper.updateCilsPlatformMapping(cilsPlatformMapping);
    }

    /**
     * 批量删除跨平台商品映射
     *
     * @param ids 需要删除的跨平台商品映射主键
     * @return 结果
     */
    @Override
    public int deleteCilsPlatformMappingByIds(Long[] ids) {
        return cilsPlatformMappingMapper.deleteCilsPlatformMappingByIds(ids);
    }

    /**
     * 删除跨平台商品映射信息
     *
     * @param id 跨平台商品映射主键
     * @return 结果
     */
    @Override
    public int deleteCilsPlatformMappingById(Long id) {
        return cilsPlatformMappingMapper.deleteCilsPlatformMappingById(id);
    }
}
