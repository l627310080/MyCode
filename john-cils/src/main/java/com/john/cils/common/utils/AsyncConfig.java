package com.john.cils.common.utils;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 定义专门用于平台同步（如 Amazon）的异步执行器
     * 采用有界队列 + 调用者运行策略，确保高并发下任务不丢失
     */
    @Bean(name = "platformSyncExecutor")
    public Executor platformSyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程数
        executor.setCorePoolSize(10);

        // 最大线程数
        executor.setMaxPoolSize(50);

        // 队列容量：在内存中排队等待的任务数————您有10000条新的订单，请您接单啦~
        // 值不能设置太高，因为万一出点问题（断电或者直接OOM啥的），一波全丢了，就算不丢，那也有可能导致http超时
        //  但又不能配置太小，不然会频繁招募临时工
        executor.setQueueCapacity(10000);

        // 线程前缀：方便在日志或监控中识别
        executor.setThreadNamePrefix("sku-sync-");

        /**
         * 设置为不允许丢数据
         * CallerRunsPolicy: 当线程池和队列都满了，任务将由提交任务的线程（即 Web HTTP 线程）执行
         * AbortPolicy（默认）：直接罢工，报错
         * DiscardPolicy：接不住的任务就不做了，直接扔掉
         * 这种设计会减缓主接口响应速度，但保证了每一个任务都会被执行
         * 意思就是：我这边最多就50个线程，它们忙得过来就让它们忙，忙不过来就你（tomcat过来请求的线程）自己干
         * 这就导致，如果tomcat的线程（200~800）如果送来太多，这边忙不过来了，那tomcat的线程就被迫打两份工，Java那边送过来的速度就被迫放缓
         * 总的来说就是：舍弃了一些效率，来保证数据的一致，如果嫌效率太低，可以充点钱，搞搞配置，或者做数据库集群
         */
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 确保服务重启时，队列里的员工要把活干完了才能走
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 60秒应该够了
        executor.setAwaitTerminationSeconds(60);
        // 启动
        executor.initialize();
        return executor;
    }
}
