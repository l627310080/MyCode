package com.john.cils.domain.dto;

import java.math.BigDecimal;

/**
 * 目标市场数据传输对象
 * 用于 SKU 一键铺货时接收前端参数
 *
 * @author john
 * @date 2026-01-10
 */
public class TargetMarketDTO {
    /** 平台映射ID (用于更新) */
    private Long id;
    
    /** 平台类型 (AMAZON, SHOPEE) */
    private String platform;
    
    /** 目标国家 (US, UK, TH) */
    private String country;
    
    /** 最终售价 */
    private BigDecimal price;
    
    /** 币种 */
    private String currency;
    
    /** 定价模式 (FIXED, MULTIPLIER) */
    private String priceMode;
    
    /** 数值 (价格或倍数) */
    private BigDecimal inputValue;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPriceMode() {
        return priceMode;
    }

    public void setPriceMode(String priceMode) {
        this.priceMode = priceMode;
    }

    public BigDecimal getInputValue() {
        return inputValue;
    }

    public void setInputValue(BigDecimal inputValue) {
        this.inputValue = inputValue;
    }
}
