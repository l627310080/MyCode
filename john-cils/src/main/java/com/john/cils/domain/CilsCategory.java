package com.john.cils.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 商品类目对象 cils_category
 *
 * @author john
 * @date 2026-01-09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CilsCategory extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 类目ID
     */
    private Long categoryId;

    /**
     * 父类目ID
     */
    @Excel(name = "父类目ID")
    private Long parentId;

    /**
     * 类目名称
     */
    @Excel(name = "类目名称")
    private String categoryName;

    /**
     * 显示顺序
     */
    @Excel(name = "显示顺序")
    private Integer orderNum;

    /**
     * 状态（0正常 1停用）
     */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /**
     * 删除标志（0存在 2删除）
     */
    private String delFlag;

    /**
     * 子类目列表
     */
    @Builder.Default
    private List<CilsCategory> children = new ArrayList<>();
}
