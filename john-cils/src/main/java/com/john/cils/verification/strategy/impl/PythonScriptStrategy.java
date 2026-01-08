package com.john.cils.verification.strategy.impl;

import com.john.cils.common.constant.CilsConstants;
import com.john.cils.domain.CilsProductSpu;
import com.john.cils.domain.CilsPythonRule;
import com.john.cils.verification.strategy.VerificationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Python 脚本校验策略实现类
 * <p>
 * 该类实现了 VerificationStrategy 接口，专门负责处理类型为 "PYTHON" 的校验规则。
 * 它通过 Java 的 ProcessBuilder 启动外部 Python 进程，将商品数据传递给脚本，
 * 并解析脚本的标准输出以判断校验是否通过。
 *
 * @author john
 * @date 2026-01-06
 */
@Component // 将该类注册为 Spring Bean，以便 VerificationContext 能够自动发现并管理它
public class PythonScriptStrategy implements VerificationStrategy {

    private static final Logger log = LoggerFactory.getLogger(PythonScriptStrategy.class);

    /**
     * 返回当前策略支持的类型标识
     * <p>
     * 这里返回 "PYTHON"，表示该类专门处理 Python 脚本类型的规则。
     */
    @Override
    public String getStrategyType() {
        return CilsConstants.STRATEGY_TYPE_PYTHON;
    }

    /**
     * 执行 Python 脚本校验逻辑
     *
     * @param spu  待校验的商品信息
     * @param rule 校验规则配置
     * @return true-通过，false-不通过
     * @throws Exception 进程执行异常或IO异常
     */
    @Override
    public boolean verify(CilsProductSpu spu, CilsPythonRule rule) throws Exception {
        log.info("开始执行Python校验策略，SPU: {}, 规则: {}", spu.getSpuCode(), rule.getRuleName());

        // 1. 构建命令行参数列表
        // 最终执行的命令格式例如：python /path/to/script.py "Product Name" "SPU001"
        List<String> command = new ArrayList<>();
        command.add(CilsConstants.CMD_PYTHON); // 添加 Python 解释器命令
        command.add(rule.getScriptPath());     // 添加脚本的绝对路径
        command.add(spu.getProductName());     // 传递参数1：商品名称
        command.add(spu.getSpuCode());         // 传递参数2：SPU编码

        // 2. 创建进程构建器
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        // 将标准错误流 (stderr) 合并到标准输出流 (stdout)
        // 这样做的好处是只需要读取一个流即可获取脚本的所有输出信息（包括报错信息）
        processBuilder.redirectErrorStream(true);

        Process process = null;
        try {
            // 3. 启动子进程执行脚本（开启新进程执行python代码并获得结果）
            process = processBuilder.start();

            // 4. 监控python运行情况
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                // 逐行读取python脚本的输出内容
                while ((line = reader.readLine()) != null) {
                    log.info("[Python Script Output]: {}", line); // 记录脚本输出日志，便于调试

                    // 解析python脚本输出协议：如果包含 "PASS" 则视为通过
                    if (line.contains(CilsConstants.SCRIPT_OUT_PASS)) {
                        return true;
                    }
                    // 解析python脚本输出协议：如果包含 "BLOCK" 则视为拦截
                    if (line.contains(CilsConstants.SCRIPT_OUT_BLOCK)) {
                        return false;
                    }
                }
            }

            // 5. 等待进程结束，并设置最大超时时间（例如 30 秒）
            // 防止脚本死循环或长时间阻塞导致 Java 线程资源耗尽
            boolean exitCode = process.waitFor(30, TimeUnit.SECONDS);
            if (!exitCode) {
                log.error("Python脚本执行超时，强制终止");
                process.destroy(); // 强制杀掉超时的 Python 进程
                return false; // 超时默认视为校验不通过
            }

        } catch (Exception e) {
            log.error("执行Python脚本异常", e);
            throw e; // 将异常抛出给上层处理
        } finally {
            // 兜底操作：确保进程对象被销毁
            if (process != null && process.isAlive()) {
                process.destroy();
            }
        }

        // 如果脚本正常结束但没有输出明确的 PASS 或 BLOCK 信号，默认返回 false 以保证安全
        return false;
    }
}
