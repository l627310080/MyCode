package com.john.cils.service.push.impl;

import com.john.cils.domain.CilsPlatformMapping;
import com.john.cils.service.push.IPushStrategy;
import com.john.cils.utils.platform.AmazonPushUtils;
import org.springframework.stereotype.Component;

/**
 * Amazon 推送策略
 *
 * @author john
 * @date 2026-01-09
 */
@Component
public class AmazonPushStrategy implements IPushStrategy {

    @Override
    public String getPlatformType() {
        return "AMAZON";
    }

    @Override
    public boolean push(CilsPlatformMapping mapping) {
        // 修正：使用 getSalePrice() 替代已废弃的 getSalePriceUsd()
        // 将 BigDecimal 转换为 double 传递给工具类
        return AmazonPushUtils.pushProduct(mapping.getPlatformSku(), mapping.getSalePrice().doubleValue());
    }
}
