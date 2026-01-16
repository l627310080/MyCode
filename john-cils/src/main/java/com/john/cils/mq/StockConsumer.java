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

    /**
     * 消费库存扣减消息
     *
     * @param message 消息内容 (JSON 字符串)
     * @param ack     手动确认对象，用于在处理成功后通知 Kafka
     */
    @KafkaListener(topics = "stock-deduct-topic", groupId = "cils-group")
    public void handleStockDeduct(String message, Acknowledgment ack) {
        log.info(">>> 收到 Kafka 库存扣减消息: {}", message);

        try {
            // 1. 解析消息
            JSONObject msgObj = JSON.parseObject(message);
            Long skuId = msgObj.getLong("skuId");
            Long remainingStock = msgObj.getLong("remainingStock");

            // 确保消息内容有效
            if (skuId != null && remainingStock != null) {
                // 2. 更新数据库
                // 创建一个只包含 ID 和库存的对象，用于更新
                CilsProductSku skuToUpdate = new CilsProductSku();
                skuToUpdate.setId(skuId);
                skuToUpdate.setStockQty(remainingStock);
                
                // 执行数据库更新操作
                // 这里是直接覆盖库存，而不是减去库存，保证了操作的幂等性
                // 即使消息被重复消费，数据库的最终结果也是正确的
                skuMapper.updateCilsProductSku(skuToUpdate);

                log.info(">>> MySQL 数据库库存更新成功。SKU: {}, 最新库存: {}", skuId, remainingStock);
            } else {
                log.warn("收到的 Kafka 消息格式不正确，已忽略: {}", message);
            }

            // 3. 手动确认消息
            // 告诉 Kafka 这条消息已经成功处理，可以从队列中移除了
            ack.acknowledge();
            log.info("<<< Kafka 消息已确认");

        } catch (Exception e) {
            log.error("消费库存消息时发生异常，将等待 Kafka 自动重试", e);
            // 注意：这里没有调用 ack.acknowledge()
            // Kafka 在检测到消息未被确认后，会在一段时间后重新投递这条消息
            // 实际生产中，如果持续失败，需要配置死信队列 (DLQ) 来处理这些“毒丸”消息
        }
    }
}
