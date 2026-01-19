package com.john.cils.service.impl;

import com.john.cils.common.constant.CilsConstants;
import com.john.cils.domain.CilsCategory;
import com.john.cils.domain.CilsProductSpu;
import com.john.cils.mapper.CilsProductSpuMapper;
import com.john.cils.service.ICilsCategoryService;
import com.john.cils.service.ICilsProductSpuService;
import com.john.cils.verification.async.AsyncVerificationService;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 跨境商品标准信息表(SPU)Service业务层处理
 *
 * @author john
 * @date 2024-05-20
 */
@Service
public class CilsProductSpuServiceImpl implements ICilsProductSpuService {
    @Autowired
    private CilsProductSpuMapper cilsProductSpuMapper;

    @Autowired
    private AsyncVerificationService asyncVerificationService;

    @Autowired
    private ICilsCategoryService categoryService; // 注入类目服务

    @Override
    public CilsProductSpu selectCilsProductSpuById(Long id) {
        return cilsProductSpuMapper.selectCilsProductSpuById(id);
    }

    @Override
    public List<CilsProductSpu> selectCilsProductSpuList(CilsProductSpu cilsProductSpu) {
        return cilsProductSpuMapper.selectCilsProductSpuList(cilsProductSpu);
    }

    @Override
    @Transactional
    public int insertCilsProductSpu(CilsProductSpu cilsProductSpu) {
        String spuCode = "SPU" + cilsProductSpu.getCategoryId() + System.currentTimeMillis();
        cilsProductSpu.setSpuCode(spuCode);

        cilsProductSpu.setCreateTime(DateUtils.getNowDate());
        cilsProductSpu.setIsAudit(CilsConstants.AUDIT_STATUS_WAITING);

        int rows = cilsProductSpuMapper.insertCilsProductSpu(cilsProductSpu);

        if (rows > 0) {
            // 填充类目名称，以便 AI 校验
            fillCategoryName(cilsProductSpu);
            asyncVerificationService.triggerVerification(cilsProductSpu);
        }

        return rows;
    }

    @Override
    @Transactional
    public int updateCilsProductSpu(CilsProductSpu cilsProductSpu) {
        cilsProductSpu.setUpdateTime(DateUtils.getNowDate());
        cilsProductSpu.setIsAudit(CilsConstants.AUDIT_STATUS_WAITING);

        int rows = cilsProductSpuMapper.updateCilsProductSpu(cilsProductSpu);

        if (rows > 0) {
            // 填充类目名称，以便 AI 校验
            fillCategoryName(cilsProductSpu);
            asyncVerificationService.triggerVerification(cilsProductSpu);
        }

        return rows;
    }

    /**
     * 辅助方法：根据 categoryId 查询并填充 categoryName
     */
    private void fillCategoryName(CilsProductSpu spu) {
        // 如果类目ID存在，通过 Service 查询 cils_category 表获取类目名称
        if (spu.getCategoryId() != null) {
            CilsCategory category = categoryService.selectCilsCategoryByCategoryId(spu.getCategoryId());
            if (category != null)
                spu.setCategoryName(category.getCategoryName());
        }
    }

    @Override
    @Transactional
    public int deleteCilsProductSpuByIds(Long[] ids) {
        return cilsProductSpuMapper.deleteCilsProductSpuByIds(ids);
    }

    @Override
    @Transactional
    public int deleteCilsProductSpuById(Long id) {
        return cilsProductSpuMapper.deleteCilsProductSpuById(id);
    }
}
