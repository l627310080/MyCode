package com.john.cils.mq;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.john.cils.domain.CilsProductSku;
import com.john.cils.mapper.CilsProductSkuMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 库存扣减消息消费者
 * <p>
 * 作用：
 * 监听 Kafka 中的库存扣减消息，异步更新数据库。
 * 这是 "Redis 扣减 -> MQ -> MySQL 更新" 架构中的最后一步，实现了流量削峰和最终一致性。
 * <p>
 * 注解说明:
 * - @Component: 注册为 Spring Bean。
 * - @KafkaListener: 监听指定的 Kafka 主题。
 *
 * @author john
 * @date 2026-01-06
 */
@Component
public class StockConsumer {

    private static final Logger log = LoggerFactory.getLogger(StockConsumer.class);
    @Autowired
    private CilsProductSkuMapper skuMapper;

    @javax.annotation.PostConstruct
    public void init() {
        log.info(">>>> [DIAGNOSTIC] StockConsumer Bean 成功加载，准备监听 Kafka...");
    }

    /**
     * 消费库存扣减消息
     *
     * @param messages 消息list (JSON 字符串)
     * @param ack      手动确认对象，用于在处理成功后通知 Kafka
     */
    @KafkaListener(topics = "stock-deduct-topic", groupId = "cils-group", containerFactory = "batchFactory")
    public void handleStockDeductBatch(List<String> messages, Acknowledgment ack) {
        long startTime = System.currentTimeMillis();
        // 使用 Map 去重，Key 是 skuId，Value 是最新的 sku 对象
        Map<Long, CilsProductSku> latestUpdateMap = new HashMap<>();

        try {
            // 循环消息并进行库存扣减
            for (String message : messages) {
                JSONObject msgObj = JSON.parseObject(message);
                Long skuId = msgObj.getLong("skuId");
                Long remainingStock = msgObj.getLong("remainingStock");

                // 先筛查无效数据并封装数据——这里是直接替换余额，而不是-1，保证数据无误
                if (skuId != null && remainingStock != null) {
                    CilsProductSku sku = new CilsProductSku();
                    sku.setId(skuId);
                    sku.setStockQty(remainingStock);
                    latestUpdateMap.put(skuId, sku);
                }
            }

            // 批量提交数据库
            if (!latestUpdateMap.isEmpty()) {
                // 把map去重后的结果放入数据库
                List<CilsProductSku> updateList = new ArrayList<>(latestUpdateMap.values());
                skuMapper.updateBatchStock(updateList);
            }

            // 确认签收——告诉kafka这批数据已正确处理
            ack.acknowledge();

            // 统计日志：每处理一批（如500条）才打印一次
            long duration = System.currentTimeMillis() - startTime;
            log.info(">>> [BATCH SUCCESS] 原始消息: {} 条, 聚合更新: {} 条, 耗时: {}ms",
                    messages.size(), latestUpdateMap.size(), duration);

        } catch (Exception e) {
            log.error("批量消费异常", e);
        }
    }
}
