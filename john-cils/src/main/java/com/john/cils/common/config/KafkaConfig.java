package com.john.cils.common.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.Map;

@Configuration
public class KafkaConfig {

    @Autowired
    private KafkaProperties kafkaProperties;

    /**
     * 定义批量消费工厂
     * 对应 @KafkaListener(containerFactory = "batchFactory")
     */
    @Bean("batchFactory")
    public ConcurrentKafkaListenerContainerFactory<String, String> batchFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        // 1. 设置消费者工厂（使用 Spring Boot 默认配置）
        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(kafkaProperties.buildConsumerProperties()));

        // 2. 核心：显式开启批量监听
        factory.setBatchListener(true);

        // 3. 设置手动提交模式（代码中的 ack.acknowledge()）
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

        // 4. 并发数（根据 CPU 核心数设置，通常 3-5 即可）
        factory.setConcurrency(3);

        return factory;
    }
}
