package com.john.cils.domain;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.*;

import java.math.BigDecimal;

/**
 * 跨平台商品映射对象 cils_platform_mapping
 *
 * @author ruoyi
 * @date 2026-01-06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CilsPlatformMapping extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 关联的本地SKU主键ID
     */
    @Excel(name = "关联的本地SKU主键ID")
    private Long skuId;

    /**
     * 目标平台标识
     */
    @Excel(name = "目标平台标识")
    private String platformType;

    /**
     * 平台SKU (Seller SKU)
     */
    @Excel(name = "平台SKU")
    private String platformSku;

    /**
     * 平台商品ID (ASIN/ItemId) - 平台返回的唯一标识
     */
    @Excel(name = "平台商品ID")
    private String platformItemId;

    /**
     * 销售价格
     */
    @Excel(name = "销售价格")
    private BigDecimal salePrice;

    /**
     * 币种
     */
    @Excel(name = "币种")
    private String currency;

    /**
     * 同步状态 (0=待同步, 1=已同步, 2=同步失败)
     */
    @Excel(name = "同步状态")
    private Long syncStatus;

    /**
     * 同步失败原因
     */
    @Excel(name = "同步失败原因")
    private String syncFailReason;

    /**
     * 目标国家
     */
    @Excel(name = "目标国家")
    private String targetCountry;

    /**
     * 逻辑删除标志
     */
    @TableLogic
    private String delFlag;
}
