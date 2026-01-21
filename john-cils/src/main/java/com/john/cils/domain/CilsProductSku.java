package com.john.cils.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.john.cils.domain.dto.TargetMarketDTO;
import com.john.cils.verification.domain.Verifiable;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品单品规格表(SKU)对象 cils_product_sku
 *
 * @author john
 * @date 2024-05-20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CilsProductSku extends BaseEntity implements Verifiable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 关联的SPU主键ID
     */
    @Excel(name = "关联的SPU主键ID")
    private Long spuId;

    /**
     * 内部SKU编码
     */
    @Excel(name = "内部SKU编码")
    private String skuCode;

    /**
     * 规格详情描述
     */
    @Excel(name = "规格详情描述")
    private String specInfo;

    /**
     * SKU规格图
     */
    @Excel(name = "SKU规格图")
    private String skuImage;

    /**
     * 当前采购成本价(人民币)
     */
    @Excel(name = "当前采购成本价(人民币)")
    private BigDecimal purchasePrice;

    /**
     * 虚拟仓储可用库存总量
     */
    @Excel(name = "虚拟仓储可用库存总量")
    private Long stockQty;

    /**
     * 商品单件重量
     */
    @Excel(name = "商品单件重量")
    private BigDecimal weightKg;

    /**
     * 审核状态
     */
    @Excel(name = "审核状态")
    private Integer isAudit;

    /**
     * 逻辑删除标志
     */
    @TableLogic
    private String delFlag;

    /**
     * 目标市场列表 (虚拟字段，用于一键铺货)
     */
    @TableField(exist = false)
    private List<TargetMarketDTO> targetMarkets;

    /**
     * 所属 SPU 名称 (虚拟字段，用于 AI 校验)
     */
    @TableField(exist = false)
    private String spuName;

    // --- Verifiable 接口实现 ---
    @Override
    public String getIdentity() {
        return "SKU:" + skuCode;
    }

    @Override
    public Long getCategoryId() {
        return null;
    }

    @Override
    public void updateAuditStatus(Integer status, String remark) {
        this.isAudit = status;
        this.setRemark(remark);
    }
}
