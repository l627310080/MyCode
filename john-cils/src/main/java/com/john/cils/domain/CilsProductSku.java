package com.john.cils.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.math.BigDecimal;

/**
 * 商品单品规格表(SKU)对象 cils_product_sku
 *
 * @author john
 * @date 2024-05-20
 */
public class CilsProductSku extends BaseEntity {
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
     * 内部SKU编码，对应实物条码
     */
    @Excel(name = "内部SKU编码")
    private String skuCode;

    /**
     * 规格详情描述，例如：颜色:午夜蓝;尺寸:42码
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
     * 商品单件重量，单位:KG
     */
    @Excel(name = "商品单件重量，单位:KG")
    private BigDecimal weightKg;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSpuId() {
        return spuId;
    }

    public void setSpuId(Long spuId) {
        this.spuId = spuId;
    }

    public String getSkuCode() {
        return skuCode;
    }

    public void setSkuCode(String skuCode) {
        this.skuCode = skuCode;
    }

    public String getSpecInfo() {
        return specInfo;
    }

    public void setSpecInfo(String specInfo) {
        this.specInfo = specInfo;
    }

    public String getSkuImage() {
        return skuImage;
    }

    public void setSkuImage(String skuImage) {
        this.skuImage = skuImage;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public Long getStockQty() {
        return stockQty;
    }

    public void setStockQty(Long stockQty) {
        this.stockQty = stockQty;
    }

    public BigDecimal getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(BigDecimal weightKg) {
        this.weightKg = weightKg;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("spuId", getSpuId())
                .append("skuCode", getSkuCode())
                .append("specInfo", getSpecInfo())
                .append("skuImage", getSkuImage())
                .append("purchasePrice", getPurchasePrice())
                .append("stockQty", getStockQty())
                .append("weightKg", getWeightKg())
                .append("createTime", getCreateTime())
                .toString();
    }
}
