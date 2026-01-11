package com.john.cils.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.http.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

/**
 * 汇率服务
 * <p>
 * 负责获取和缓存货币汇率。
 * 包含定时任务，每日自动从外部 API 更新汇率。
 * 增加了重试机制和兜底策略，保证系统稳定性。
 *
 * @author john
 * @date 2026-01-10
 */
@Service
public class ExchangeRateService {

    private static final Logger log = LoggerFactory.getLogger(ExchangeRateService.class);

    @Autowired
    private RedisCache redisCache;

    // 汇率缓存前缀
    private static final String RATE_KEY_PREFIX = "exchange_rate:";
    
    // 免费汇率 API
    private static final String API_URL = "https://api.exchangerate-api.com/v4/latest/CNY";

    /**
     * 获取指定货币对人民币的汇率
     *
     * @param currencyCode 货币代码 (如 USD)
     * @return 汇率 (1 CNY = ? 外币) 注意：这里返回的是 1 人民币兑换多少外币
     */
    public BigDecimal getRate(String currencyCode) {
        String key = RATE_KEY_PREFIX + currencyCode;
        BigDecimal rate = redisCache.getCacheObject(key);
        
        if (rate == null) {
            // 如果缓存没有，尝试即时获取一次
            refreshRates();
            rate = redisCache.getCacheObject(key);
        }
        
        // 兜底策略：如果还是取不到，返回硬编码的默认值
        if (rate == null) {
            log.warn("无法获取汇率，使用兜底默认值: {}", currencyCode);
            return getDefaultRate(currencyCode);
        }
        
        return rate;
    }

    /**
     * 定时任务：每天凌晨 1 点刷新汇率
     * 增加重试机制：如果失败，重试 3 次，每次间隔 5 秒
     */
    @Scheduled(cron = "0 0 1 * * ?")
    @Retryable(value = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 5000))
    public void refreshRates() {
        log.info("开始同步每日汇率...");
        String result = HttpUtils.sendGet(API_URL);
        JSONObject json = JSON.parseObject(result);
        JSONObject rates = json.getJSONObject("rates");

        if (rates != null) {
            // 存入 Redis，设置 25 小时过期
            updateRate("USD", rates);
            updateRate("GBP", rates); // 英镑
            updateRate("THB", rates); // 泰铢
            updateRate("JPY", rates); // 日元
            log.info("汇率同步完成");
        } else {
            throw new RuntimeException("API 返回数据异常");
        }
    }
    
    @Recover
    public void recover(Exception e) {
        log.error("汇率同步最终失败，请检查网络或 API 服务", e);
    }

    private void updateRate(String currency, JSONObject rates) {
        BigDecimal rate = rates.getBigDecimal(currency);
        if (rate != null) {
            redisCache.setCacheObject(RATE_KEY_PREFIX + currency, rate, 25, TimeUnit.HOURS);
        }
    }
    
    /**
     * 获取兜底汇率 (1 CNY = ? 外币)
     */
    private BigDecimal getDefaultRate(String currency) {
        switch (currency) {
            case "USD": return new BigDecimal("0.14");
            case "GBP": return new BigDecimal("0.11");
            case "THB": return new BigDecimal("5.0");
            case "JPY": return new BigDecimal("20.0");
            default: return BigDecimal.ONE;
        }
    }
}
