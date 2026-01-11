package com.john.cils.utils.python;

import com.john.cils.common.constant.CilsConstants;
import com.john.cils.verification.domain.VerificationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Python 脚本执行工具类
 *
 * @author john
 * @date 2026-01-09
 */
public class PythonRunnerUtils {

    private static final Logger log = LoggerFactory.getLogger(PythonRunnerUtils.class);

    /**
     * 执行 Python 脚本
     *
     * @param relativeScriptPath 脚本相对路径
     * @param jsonData           传递给脚本的 JSON 数据
     * @return 校验结果
     */
    public static VerificationResult runScript(String relativeScriptPath, String jsonData) {
        // 关键日志：打印将要传递给 Python 的完整 JSON
        log.info("向 Python 脚本传递的 JSON 数据: {}", jsonData);

        String projectRoot = System.getProperty("user.dir");
        String absoluteScriptPath = Paths.get(projectRoot, relativeScriptPath).toString();

        List<String> command = new ArrayList<>();
        command.add(CilsConstants.CMD_PYTHON);
        command.add(absoluteScriptPath);

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);

        Process process = null;
        String failReason = "未知错误";
        boolean isPass = false;

        try {
            process = processBuilder.start();

            try (Writer writer = new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8)) {
                writer.write(jsonData);
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info("[Python Output]: {}", line);
                    if (line.startsWith("FAIL_REASON:")) {
                        failReason = line.substring("FAIL_REASON:".length());
                    }
                    if (line.contains(CilsConstants.SCRIPT_OUT_PASS)) {
                        isPass = true;
                    }
                    if (line.contains(CilsConstants.SCRIPT_OUT_BLOCK)) {
                        isPass = false;
                    }
                }
            }

            boolean exitCode = process.waitFor(30, TimeUnit.SECONDS);
            if (!exitCode) {
                process.destroy();
                return VerificationResult.fail("脚本执行超时 (30s)");
            }

        } catch (Exception e) {
            log.error("执行Python脚本异常", e);
            return VerificationResult.fail("系统异常: " + e.getMessage());
        } finally {
            if (process != null && process.isAlive()) {
                process.destroy();
            }
        }

        if (isPass) {
            return VerificationResult.pass();
        } else {
            return VerificationResult.fail(failReason);
        }
    }
}
