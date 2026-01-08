package com.john.cils.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 跨境商品标准信息(SPU)对象 cils_product_spu
 *
 * @author ruoyi
 * @date 2026-01-06
 */
public class CilsProductSpu extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 内部唯一SPU编码，用于仓库管理
     */
    @Excel(name = "内部唯一SPU编码，用于仓库管理")
    private String spuCode;

    /**
     * 商品标准标题，用于多语言翻译基础
     */
    @Excel(name = "商品标准标题，用于多语言翻译基础")
    private String productName;

    /**
     * 系统内部类目ID
     */
    @Excel(name = "系统内部类目ID")
    private Long categoryId;

    /**
     * 商品展示主图网络地址
     */
    @Excel(name = "商品展示主图网络地址")
    private String mainImage;

    /**
     * 审核状态：0-待审核，1-审核通过，2-合规拦截
     */
    @Excel(name = "审核状态：0-待审核，1-审核通过，2-合规拦截")
    private Integer isAudit;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSpuCode() {
        return spuCode;
    }

    public void setSpuCode(String spuCode) {
        this.spuCode = spuCode;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getMainImage() {
        return mainImage;
    }

    public void setMainImage(String mainImage) {
        this.mainImage = mainImage;
    }

    public Integer getIsAudit() {
        return isAudit;
    }

    public void setIsAudit(Integer isAudit) {
        this.isAudit = isAudit;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("spuCode", getSpuCode())
                .append("productName", getProductName())
                .append("categoryId", getCategoryId())
                .append("mainImage", getMainImage())
                .append("isAudit", getIsAudit())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}
