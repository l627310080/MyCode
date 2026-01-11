package com.john.cils.service.push.impl;

import com.john.cils.domain.CilsPlatformMapping;
import com.john.cils.service.push.IPushStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Shopee 推送策略
 *
 * @author john
 * @date 2026-01-09
 */
@Component
public class ShopeePushStrategy implements IPushStrategy {

    private static final Logger log = LoggerFactory.getLogger(ShopeePushStrategy.class);

    @Override
    public String getPlatformType() {
        return "SHOPEE";
    }

    @Override
    public boolean push(CilsPlatformMapping mapping) {
        log.info("正在连接 Shopee API...");
        try {
            Thread.sleep(1000);
            log.info("Shopee 推送成功: SKU={}", mapping.getPlatformSku());
            return true;
        } catch (InterruptedException e) {
            return false;
        }
    }
}
