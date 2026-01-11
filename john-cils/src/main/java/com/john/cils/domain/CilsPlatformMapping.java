package com.john.cils.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.math.BigDecimal;

/**
 * 跨平台商品映射对象 cils_platform_mapping
 *
 * @author ruoyi
 * @date 2026-01-06
 */
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
     * 目标平台标识，如：AMAZON, SHOPEE, TIKTOK
     */
    @Excel(name = "目标平台标识，如：AMAZON, SHOPEE, TIKTOK")
    private String platformType;

    /**
     * 该商品在对应平台上的刊登SKU编码
     */
    @Excel(name = "该商品在对应平台上的刊登SKU编码")
    private String platformSku;

    /**
     * 该平台的外币销售价格(美元)
     */
    @Excel(name = "该平台的外币销售价格(美元)")
    private BigDecimal salePriceUsd;

    /**
     * 平台同步状态：1-已同步，0-需重新推送
     */
    @Excel(name = "平台同步状态：1-已同步，0-需重新推送")
    private Long syncStatus;

    /**
     * 目标国家 (US, UK, JP)
     */
    @Excel(name = "目标国家")
    private String targetCountry;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }

    public String getPlatformType() {
        return platformType;
    }

    public void setPlatformType(String platformType) {
        this.platformType = platformType;
    }

    public String getPlatformSku() {
        return platformSku;
    }

    public void setPlatformSku(String platformSku) {
        this.platformSku = platformSku;
    }

    public BigDecimal getSalePriceUsd() {
        return salePriceUsd;
    }

    public void setSalePriceUsd(BigDecimal salePriceUsd) {
        this.salePriceUsd = salePriceUsd;
    }

    public Long getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(Long syncStatus) {
        this.syncStatus = syncStatus;
    }

    public String getTargetCountry() {
        return targetCountry;
    }

    public void setTargetCountry(String targetCountry) {
        this.targetCountry = targetCountry;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("skuId", getSkuId())
                .append("platformType", getPlatformType())
                .append("platformSku", getPlatformSku())
                .append("salePriceUsd", getSalePriceUsd())
                .append("syncStatus", getSyncStatus())
                .append("targetCountry", getTargetCountry())
                .append("createTime", getCreateTime())
                .toString();
    }
}
