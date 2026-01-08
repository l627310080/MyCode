package com.john.cils.verification.context;

import com.john.cils.domain.CilsProductSpu;
import com.john.cils.domain.CilsPythonRule;
import com.john.cils.verification.strategy.VerificationStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 校验策略上下文 (Context)
 * <p>
 * 负责根据规则配置中的策略类型，动态路由到具体的策略实现类。
 *
 * @author john
 * @date 2026-01-06
 */
@Component
public class VerificationContext {

    private final Map<String, VerificationStrategy> strategyMap = new ConcurrentHashMap<>();

    /**
     * 自动注入所有 VerificationStrategy 的实现类
     *
     * @param strategies Spring 容器中所有的策略 Bean
     */
    @Autowired
    public VerificationContext(Map<String, VerificationStrategy> strategies) {
        strategies.forEach((k, v) -> {
            this.strategyMap.put(v.getStrategyType(), v);
        });
    }

    /**
     * 执行校验
     *
     * @param spu  商品信息
     * @param rule 规则信息
     * @return 校验结果
     */
    public boolean executeStrategy(CilsProductSpu spu, CilsPythonRule rule) throws Exception {
        // 直接从数据库配置中获取策略类型 (例如 "PYTHON", "REGEX")
        // 彻底移除了硬编码判断，符合开闭原则
        String strategyType = rule.getStrategyType();

        // 获取对应的策略类型
        VerificationStrategy strategy = strategyMap.get(strategyType);

        if (strategy == null)
            throw new RuntimeException("未找到对应的校验策略: " + strategyType + "，请检查数据库配置 [strategy_type] 字段");

        // 校验对象和规则传入，返回是否校验通过
        return strategy.verify(spu, rule);
    }
}
