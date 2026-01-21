package com.john.cils.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 目标市场数据传输对象
 * 用于 SKU 一键铺货时接收前端参数
 *
 * @author john
 * @date 2026-01-10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TargetMarketDTO {
    /**
     * 平台映射ID (用于更新)
     */
    private Long id;

    /**
     * 平台类型 (AMAZON, SHOPEE)
     */
    private String platform;

    /**
     * 目标国家 (US, UK, TH)
     */
    private String country;

    /**
     * 最终售价
     */
    private BigDecimal price;

    /**
     * 币种
     */
    private String currency;

    /**
     * 定价模式 (FIXED, MULTIPLIER)
     */
    private String priceMode;

    /**
     * 数值 (价格或倍数)
     */
    private BigDecimal inputValue;
}
