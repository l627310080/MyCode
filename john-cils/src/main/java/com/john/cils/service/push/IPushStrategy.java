package com.john.cils.service.push;

import com.john.cils.domain.CilsPlatformMapping;

/**
 * 平台推送策略接口
 *
 * @author john
 * @date 2026-01-09
 */
public interface IPushStrategy {

    /**
     * 获取支持的平台类型
     *
     * @return 例如 "AMAZON", "SHOPEE"
     */
    String getPlatformType();

    /**
     * 执行推送逻辑
     *
     * @param mapping 映射信息
     * @return 是否成功
     */
    boolean push(CilsPlatformMapping mapping);
}
