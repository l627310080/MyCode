package com.john.cils.domain;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.john.cils.verification.domain.Verifiable;
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
public class CilsProductSpu extends BaseEntity implements Verifiable {
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** SPU编码 */
    @Excel(name = "SPU编码")
    private String spuCode;

    /** 商品标题 */
    @Excel(name = "商品标题")
    private String productName;

    /** 类目ID */
    @Excel(name = "类目ID")
    private Long categoryId;

    /** 类目名称 (非数据库字段，仅用于展示) */
    @Excel(name = "类目名称")
    private String categoryName;

    /** 主图 */
    @Excel(name = "主图")
    private String mainImage;

    /** 审核状态 */
    @Excel(name = "审核状态")
    private Integer isAudit;

    /** 逻辑删除标志 (0代表存在 2代表删除) */
    @TableLogic
    private String delFlag;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSpuCode() { return spuCode; }
    public void setSpuCode(String spuCode) { this.spuCode = spuCode; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getMainImage() { return mainImage; }
    public void setMainImage(String mainImage) { this.mainImage = mainImage; }

    public Integer getIsAudit() { return isAudit; }
    public void setIsAudit(Integer isAudit) { this.isAudit = isAudit; }

    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }

    // --- Verifiable 接口实现 ---
    @Override
    public String getIdentity() { return "SPU:" + spuCode; }

    @Override
    public void updateAuditStatus(Integer status, String remark) {
        this.isAudit = status;
        this.setRemark(remark);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("spuCode", getSpuCode())
                .append("productName", getProductName())
                .append("categoryId", getCategoryId())
                .append("categoryName", getCategoryName())
                .append("mainImage", getMainImage())
                .append("isAudit", getIsAudit())
                .toString();
    }
}
