package com.john.cils.service;

import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.http.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 汇率服务
 * <p>
 * 负责获取和缓存实时汇率。
 * 采用 "Redis 缓存 + 定时刷新 + 兜底策略" 的设计，确保高可用性。
 *
 * @author john
 * @date 2026-01-09
 */
@Service
public class ExchangeRateService {

    private static final Logger log = LoggerFactory.getLogger(ExchangeRateService.class);
    // 汇率缓存 Key 前缀
    private static final String RATE_KEY_PREFIX = "exchange_rate:";
    // 兜底汇率表 (当 API 不可用时使用)
    private static final Map<String, BigDecimal> DEFAULT_RATES = new HashMap<>();

    static {
        // 初始化兜底汇率 (1 CNY 对换多少外币)
        DEFAULT_RATES.put("USD", new BigDecimal("0.14"));
        DEFAULT_RATES.put("GBP", new BigDecimal("0.11"));
        DEFAULT_RATES.put("THB", new BigDecimal("5.00"));
        DEFAULT_RATES.put("CNY", BigDecimal.ONE);
    }

    @Autowired
    private RedisCache redisCache;
    // 从环境变量 EXCHANGERATE_API_KEY 中读取 API Key，如果找不到，则使用冒号后面的默认值
    @Value("${EXCHANGERATE_API_KEY:7f52f4df413cf6df77ba9e16}")
    private String apiKey;

    /**
     * 获取指定货币对人民币的汇率
     */
    public BigDecimal getRate(String targetCurrency) {
        if (StringUtils.isEmpty(targetCurrency)) return BigDecimal.ONE;
        targetCurrency = targetCurrency.toUpperCase();

        String key = RATE_KEY_PREFIX + targetCurrency;
        BigDecimal rate = redisCache.getCacheObject(key);

        if (rate != null) {
            return rate;
        }

        log.info("缓存未命中，尝试刷新汇率: {}", targetCurrency);
        refreshRates();

        rate = redisCache.getCacheObject(key);
        if (rate != null) {
            return rate;
        }

        log.warn("无法获取实时汇率，使用兜底默认值: {}", targetCurrency);
        return DEFAULT_RATES.getOrDefault(targetCurrency, BigDecimal.ONE);
    }

    /**
     * 刷新汇率数据
     */
    public void refreshRates() {
        log.info(">>> 开始同步每日汇率...");
        String url = "https://v6.exchangerate-api.com/v6/" + apiKey + "/latest/CNY";

        try {
            String result = HttpUtils.sendGet(url);

            if (StringUtils.isEmpty(result)) {
                log.error("汇率 API 返回为空，跳过刷新");
                return;
            }

            JSONObject json = JSONObject.parseObject(result);
            if (json == null || !"success".equals(json.getString("result"))) {
                log.error("汇率 API 返回错误: {}", result);
                return;
            }

            JSONObject rates = json.getJSONObject("conversion_rates");
            if (rates == null) {
                log.error("汇率 API 返回格式错误，找不到 conversion_rates 字段");
                return;
            }

            log.info("成功获取汇率数据，包含 {} 种货币", rates.size());

            updateCache("USD", rates);
            updateCache("GBP", rates);
            updateCache("THB", rates);

            log.info("<<< 汇率同步完成");

        } catch (Exception e) {
            log.error("同步汇率时发生异常", e);
        }
    }

    /**
     * 更新单个货币的缓存
     */
    private void updateCache(String currency, JSONObject rates) {
        if (rates.containsKey(currency)) {
            BigDecimal rate = rates.getBigDecimal(currency);
            redisCache.setCacheObject(RATE_KEY_PREFIX + currency, rate, 24, TimeUnit.HOURS);
            log.info("更新汇率缓存: CNY -> {} = {}", currency, rate);
        }
    }
}
