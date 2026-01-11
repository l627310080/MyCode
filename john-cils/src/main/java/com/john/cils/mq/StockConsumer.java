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
     * @param ack     手动确认对象
     */
    @KafkaListener(topics = "stock-deduct-topic", groupId = "cils-group")
    public void handleStockDeduct(String message, Acknowledgment ack) {
        log.info("收到库存扣减消息: {}", message);

        try {
            // 1. 解析消息
            JSONObject msgObj = JSON.parseObject(message);
            Long skuId = msgObj.getLong("skuId");
            Long remainingStock = msgObj.getLong("remainingStock");

            if (skuId != null && remainingStock != null) {
                // 2. 更新数据库
                CilsProductSku skuToUpdate = new CilsProductSku();
                skuToUpdate.setId(skuId);
                skuToUpdate.setStockQty(remainingStock);
                skuMapper.updateCilsProductSku(skuToUpdate);

                log.info("数据库库存更新成功。SKU: {}, 最新库存: {}", skuId, remainingStock);
            }

            // 3. 手动确认消息 (告诉 Kafka 这条消息处理完了，可以删了)
            ack.acknowledge();

        } catch (Exception e) {
            log.error("消费库存消息失败", e);
            // 注意：这里没有 ack，Kafka 会在稍后重新投递这条消息 (重试机制)
            // 实际生产中可能需要死信队列 (DLQ) 处理一直失败的消息
        }
    }
}
