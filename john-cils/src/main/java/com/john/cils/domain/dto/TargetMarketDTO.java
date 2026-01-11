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
    /** 平台类型 (AMAZON, SHOPEE) */
    private String platform;
    
    /** 目标国家 (US, UK, TH) */
    private String country;
    
    /** 最终售价 */
    private BigDecimal price;

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
}
