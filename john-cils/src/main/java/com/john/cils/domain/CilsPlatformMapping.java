package com.john.cils.domain;

import com.baomidou.mybatisplus.annotation.TableLogic;
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

    /** 主键ID */
    private Long id;

    /** 关联的本地SKU主键ID */
    @Excel(name = "关联的本地SKU主键ID")
    private Long skuId;

    /** 目标平台标识 */
    @Excel(name = "目标平台标识")
    private String platformType;

    /** 平台SKU (Seller SKU) */
    @Excel(name = "平台SKU")
    private String platformSku;
    
    /** 平台商品ID (ASIN/ItemId) - 平台返回的唯一标识 */
    @Excel(name = "平台商品ID")
    private String platformItemId;

    /** 销售价格 */
    @Excel(name = "销售价格")
    private BigDecimal salePrice;
    
    /** 币种 */
    @Excel(name = "币种")
    private String currency;

    /** 同步状态 (0=待同步, 1=已同步, 2=同步失败) */
    @Excel(name = "同步状态")
    private Long syncStatus;
    
    /** 同步失败原因 */
    @Excel(name = "同步失败原因")
    private String syncFailReason;

    /** 目标国家 */
    @Excel(name = "目标国家")
    private String targetCountry;

    /** 逻辑删除标志 */
    @TableLogic
    private String delFlag;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSkuId() { return skuId; }
    public void setSkuId(Long skuId) { this.skuId = skuId; }

    public String getPlatformType() { return platformType; }
    public void setPlatformType(String platformType) { this.platformType = platformType; }

    public String getPlatformSku() { return platformSku; }
    public void setPlatformSku(String platformSku) { this.platformSku = platformSku; }
    
    public String getPlatformItemId() { return platformItemId; }
    public void setPlatformItemId(String platformItemId) { this.platformItemId = platformItemId; }

    public BigDecimal getSalePrice() { return salePrice; }
    public void setSalePrice(BigDecimal salePrice) { this.salePrice = salePrice; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public Long getSyncStatus() { return syncStatus; }
    public void setSyncStatus(Long syncStatus) { this.syncStatus = syncStatus; }
    
    public String getSyncFailReason() { return syncFailReason; }
    public void setSyncFailReason(String syncFailReason) { this.syncFailReason = syncFailReason; }

    public String getTargetCountry() { return targetCountry; }
    public void setTargetCountry(String targetCountry) { this.targetCountry = targetCountry; }

    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("skuId", getSkuId())
                .append("platformType", getPlatformType())
                .append("platformSku", getPlatformSku())
                .append("platformItemId", getPlatformItemId())
                .append("salePrice", getSalePrice())
                .append("currency", getCurrency())
                .append("syncStatus", getSyncStatus())
                .append("syncFailReason", getSyncFailReason())
                .append("targetCountry", getTargetCountry())
                .toString();
    }
}
