package com.john.cils.service.impl;

import com.john.cils.common.constant.CilsConstants;
import com.john.cils.domain.CilsProductSpu;
import com.john.cils.mapper.CilsProductSpuMapper;
import com.john.cils.service.ICilsProductSpuService;
import com.john.cils.verification.async.AsyncVerificationService;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 跨境商品标准信息表(SPU)Service业务层处理
 * <p>
 * 该类负责处理 SPU 相关的核心业务逻辑，包括商品的增删改查。
 * 它作为业务入口，协调 Mapper 层进行数据库操作，并调用 AsyncVerificationService 触发异步合规校验。
 * <p>
 * 注解说明:
 * - @Service: 标识该类为 Spring 的 Service 组件，会被自动扫描并注册为 Bean。
 * 
 * @author john
 * @date 2024-05-20
 */
@Service
public class CilsProductSpuServiceImpl implements ICilsProductSpuService {
    @Autowired
    private CilsProductSpuMapper cilsProductSpuMapper;

    @Autowired
    private AsyncVerificationService asyncVerificationService; // 注入异步校验服务

    /**
     * 查询跨境商品标准信息表(SPU)
     *
     * @param id 跨境商品标准信息表(SPU)主键
     * @return 跨境商品标准信息表(SPU)
     */
    @Override
    public CilsProductSpu selectCilsProductSpuById(Long id) {
        return cilsProductSpuMapper.selectCilsProductSpuById(id);
    }

    /**
     * 查询跨境商品标准信息表(SPU)列表
     *
     * @param cilsProductSpu 跨境商品标准信息表(SPU)
     * @return 跨境商品标准信息表(SPU)集合
     */
    @Override
    public List<CilsProductSpu> selectCilsProductSpuList(CilsProductSpu cilsProductSpu) {
        return cilsProductSpuMapper.selectCilsProductSpuList(cilsProductSpu);
    }

    /**
     * 新增跨境商品标准信息表(SPU)
     * <p>
     * 核心逻辑：
     * 1. 自动生成 SPU 编码。
     * 2. 设置商品的初始状态为“待审核”。
     * 3. 将商品数据保存到数据库，并获取生成的 ID。
     * 4. 触发异步校验任务，不阻塞当前线程，直接返回结果。
     * <p>
     * 注解说明:
     * - @Transactional: 开启事务。如果方法执行过程中抛出异常，所有数据库操作会自动回滚。
     *
     * @param cilsProductSpu 跨境商品标准信息表(SPU)
     * @return 结果
     */
    @Override
    @Transactional
    public int insertCilsProductSpu(CilsProductSpu cilsProductSpu) {
        // 1. 自动生成 SPU 编码
        String spuCode = "SPU" + cilsProductSpu.getCategoryId() + System.currentTimeMillis();
        cilsProductSpu.setSpuCode(spuCode);
        
        cilsProductSpu.setCreateTime(DateUtils.getNowDate());
        // 设置默认状态为 0-待审核
        cilsProductSpu.setIsAudit(CilsConstants.AUDIT_STATUS_WAITING);

        // 2. 先保存到数据库，获取生成的 ID
        int rows = cilsProductSpuMapper.insertCilsProductSpu(cilsProductSpu);

        // 3. 如果保存成功
        if (rows > 0) {
            // 触发异步合规校验
            asyncVerificationService.triggerVerification(cilsProductSpu);
        }

        return rows;
    }

    /**
     * 修改跨境商品标准信息表(SPU)
     * <p>
     * 优化逻辑：
     * 1. 更新数据库。
     * 2. 重新触发异步校验 (因为标题或图片可能已修改)。
     * 3. 重置审核状态为“待审核”。
     *
     * @param cilsProductSpu 跨境商品标准信息表(SPU)
     * @return 结果
     */
    @Override
    @Transactional
    public int updateCilsProductSpu(CilsProductSpu cilsProductSpu) {
        cilsProductSpu.setUpdateTime(DateUtils.getNowDate());
        
        // 修改后需要重新审核，所以状态重置为 0-待审核
        cilsProductSpu.setIsAudit(CilsConstants.AUDIT_STATUS_WAITING);
        
        // 1. 更新数据库
        int rows = cilsProductSpuMapper.updateCilsProductSpu(cilsProductSpu);
        
        if (rows > 0) {
            // 2. 重新触发异步校验
            asyncVerificationService.triggerVerification(cilsProductSpu);
        }
        
        return rows;
    }

    /**
     * 批量删除跨境商品标准信息表(SPU)
     *
     * @param ids 需要删除的跨境商品标准信息表(SPU)主键
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteCilsProductSpuByIds(Long[] ids) {
        return cilsProductSpuMapper.deleteCilsProductSpuByIds(ids);
    }

    /**
     * 删除跨境商品标准信息表(SPU)信息
     *
     * @param id 跨境商品标准信息表(SPU)主键
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteCilsProductSpuById(Long id) {
        return cilsProductSpuMapper.deleteCilsProductSpuById(id);
    }
}
