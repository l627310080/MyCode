package com.john.cils.task;

import com.john.cils.service.ExchangeRateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 汇率定时刷新任务
 *
 * @author john
 * @date 2026-01-14
 */
@Component
public class ExchangeRateRefreshTask {

    @Autowired
    private ExchangeRateService exchangeRateService;

    /**
     * 服务启动时立即执行一次汇率刷新
     * 方便本地测试，确保汇率数据是最新的
     */
    @PostConstruct
    public void init() {
        exchangeRateService.refreshRates();
    }

    /**
     * 每天凌晨2点执行汇率刷新
     * cron 表达式: 秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void refreshRates() {
        exchangeRateService.refreshRates();
    }
}
