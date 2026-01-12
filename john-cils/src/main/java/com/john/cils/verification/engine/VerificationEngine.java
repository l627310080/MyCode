package com.john.cils.verification.engine;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.john.cils.domain.CilsRuleConfig;
import com.john.cils.utils.python.PythonRunnerUtils;
import com.john.cils.verification.domain.Verifiable;
import com.john.cils.verification.domain.VerificationResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 校验引擎核心类 (重构版)
 *
 * @author john
 * @date 2026-01-09
 */
@Component
public class VerificationEngine {

    /**
     * 批量执行校验规则
     */
    public VerificationResult executeBatch(Verifiable data, List<CilsRuleConfig> rules) {
        if (rules == null || rules.isEmpty()) {
            return VerificationResult.pass();
        }

        Map<String, Object> context = new HashMap<>();
        context.put("productData", data);
        
        // 获取商品标题，作为上下文传给 AI
        String productName = "";
        try {
            JSONObject dataJson = JSONObject.parseObject(JSON.toJSONString(data));
            productName = dataJson.getString("productName");
        } catch (Exception e) {
            // 忽略
        }
        
        List<Map<String, String>> ruleList = new ArrayList<>();
        for (CilsRuleConfig rule : rules) {
            Map<String, String> ruleMap = new HashMap<>();
            ruleMap.put("name", rule.getRuleName());
            ruleMap.put("field", rule.getTargetField());
            ruleMap.put("content", rule.getAiPrompt());
            ruleMap.put("errorMsg", rule.getErrorMessage());
            // 传递商品标题作为上下文
            ruleMap.put("productName", productName);
            
            // 自动判断类型
            String field = rule.getTargetField().toLowerCase();
            if (field.contains("image") || field.contains("img")) {
                ruleMap.put("type", "IMAGE");
            } else {
                ruleMap.put("type", "TEXT");
            }

            ruleList.add(ruleMap);
        }
        context.put("rules", ruleList);

        String jsonData = JSON.toJSONString(context);

        return PythonRunnerUtils.runScript("py_scripts/universal_checker.py", jsonData);
    }
}
