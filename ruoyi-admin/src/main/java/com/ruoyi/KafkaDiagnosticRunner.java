package com.ruoyi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaDiagnosticRunner implements CommandLineRunner {

    @Autowired(required = false)
    private KafkaTemplate<String, String> kafkaTemplate;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=================================================");
        System.out.println(">>>> [CRITICAL DIAGNOSTIC] Kafka 自动配置检查 <<<<");
        if (kafkaTemplate != null) {
            System.out.println(">>>> [SUCCESS] KafkaTemplate 已成功注入！");
            System.out.println(">>>> 目标服务器: "
                    + kafkaTemplate.getProducerFactory().getConfigurationProperties().get("bootstrap.servers"));
        } else {
            System.err.println(">>>> [FAILURE] KafkaTemplate 未能注入！Spring 可能没有加载 Kafka 配置。");
        }
        System.out.println("=================================================");
    }
}
