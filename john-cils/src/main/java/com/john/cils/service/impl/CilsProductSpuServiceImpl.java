package com.john.cils.service.impl;

import com.john.cils.common.constant.CilsConstants;
import com.john.cils.domain.CilsProductSpu;
import com.john.cils.mapper.CilsProductSpuMapper;
import com.john.cils.service.ICilsProductSpuService;
import com.john.cils.verification.async.AsyncVerificationService;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 跨境商品标准信息表(SPU)Service业务层处理
 * <p>
 * 作用：
 * 负责处理 SPU 相关的核心业务逻辑，如新增、修改、查询。
 * 它协调 Mapper 层（数据库操作）和 AsyncVerificationService（合规校验）。
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
     * @return 跨境商品标准信息表(SPU)
     */
    @Override
    public List<CilsProductSpu> selectCilsProductSpuList(CilsProductSpu cilsProductSpu) {
        return cilsProductSpuMapper.selectCilsProductSpuList(cilsProductSpu);
    }

    /**
     * 新增跨境商品标准信息表(SPU)
     * <p>
     * 核心逻辑：
     * 1. 设置初始状态为“待审核”。
     * 2. 保存数据到数据库。
     * 3. 触发异步校验任务（不阻塞）。
     *
     * @param cilsProductSpu 跨境商品标准信息表(SPU)
     * @return 结果
     */
    @Override
    public int insertCilsProductSpu(CilsProductSpu cilsProductSpu) {
        cilsProductSpu.setCreateTime(DateUtils.getNowDate());
        // 默认状态为 0-待审核
        cilsProductSpu.setIsAudit(CilsConstants.AUDIT_STATUS_WAITING);

        // 1. 先保存到数据库，获取生成的 ID
        // 这一步先做，后面的校验需要用这个 ID
        int rows = cilsProductSpuMapper.insertCilsProductSpu(cilsProductSpu);

        // 2. 如果保存成功，触发异步合规校验
        if (rows > 0) {
            // 异步调用，不会阻塞当前线程
            // 用户会立刻收到“保存成功”的响应，而校验在后台慢慢跑
            // 直接传递 SPU 对象
            asyncVerificationService.triggerVerification(cilsProductSpu);
        }

        return rows;
    }

    /**
     * 修改跨境商品标准信息表(SPU)
     *
     * @param cilsProductSpu 跨境商品标准信息表(SPU)
     * @return 结果
     */
    @Override
    public int updateCilsProductSpu(CilsProductSpu cilsProductSpu) {
        cilsProductSpu.setUpdateTime(DateUtils.getNowDate());
        return cilsProductSpuMapper.updateCilsProductSpu(cilsProductSpu);
    }

    /**
     * 批量删除跨境商品标准信息表(SPU)
     *
     * @param ids 需要删除的跨境商品标准信息表(SPU)主键
     * @return 结果
     */
    @Override
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
    public int deleteCilsProductSpuById(Long id) {
        return cilsProductSpuMapper.deleteCilsProductSpuById(id);
    }
}
