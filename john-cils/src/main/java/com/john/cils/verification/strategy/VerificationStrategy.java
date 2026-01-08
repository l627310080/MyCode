package com.john.cils.verification.strategy;

import com.john.cils.domain.CilsProductSpu;
import com.john.cils.domain.CilsPythonRule;

/**
 * 合规校验策略接口
 * <p>
 * 该接口定义了所有合规校验策略必须遵循的统一规范。
 * 任何具体的校验逻辑（如 Python 脚本校验、正则表达式校验、第三方 API 校验）都必须实现此接口。
 * 这种设计允许系统在不修改核心业务代码的情况下，动态扩展新的校验方式。
 *
 * @author john
 * @date 2026-01-06
 */
public interface VerificationStrategy {

    /**
     * 执行具体的校验逻辑
     * <p>
     * 该方法接收待校验的商品数据和校验规则，执行具体的校验算法，并返回校验结果。
     *
     * @param spu  待校验的商品信息 (数据源)
     * @param rule 校验规则配置 (包含脚本路径、参数等)
     * @return true 表示校验通过，false 表示校验不通过
     * @throws Exception 校验过程中发生的任何异常（如 IO 异常、脚本执行错误等）
     */
    boolean verify(CilsProductSpu spu, CilsPythonRule rule) throws Exception;

    /**
     * 获取策略类型的唯一标识
     * <p>
     * 该方法用于返回当前策略实现类的类型标识（例如 "PYTHON"）。
     * VerificationContext 将使用此标识将策略注册到内部映射表中，以便根据规则配置动态查找。
     *
     * @return 策略类型字符串
     */
    String getStrategyType();
}
