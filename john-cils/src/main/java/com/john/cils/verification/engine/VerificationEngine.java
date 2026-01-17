package com.john.cils.verification.engine;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.john.cils.domain.CilsProductSku;
import com.john.cils.domain.CilsProductSpu;
import com.john.cils.domain.CilsRuleConfig;
import com.john.cils.mapper.CilsProductSpuMapper;
import com.john.cils.utils.python.PythonRunnerUtils;
import com.john.cils.verification.domain.Verifiable;
import com.john.cils.verification.domain.VerificationResult;
import com.ruoyi.system.service.ISysDictDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 校验引擎核心类
 *
 * @author john
 * @date 2026-01-09
 */
@Component
public class VerificationEngine {

    private static final Logger log = LoggerFactory.getLogger(VerificationEngine.class);

    @Autowired
    private CilsProductSpuMapper spuMapper;
    
    @Autowired
    private ISysDictDataService dictDataService;

    public VerificationResult executeBatch(Verifiable data, List<CilsRuleConfig> rules) {
        if (rules == null || rules.isEmpty()) {
            return VerificationResult.pass();
        }

        log.info(">>> 开始执行校验引擎，对象: {}, 规则数量: {}", data.getIdentity(), rules.size());

        Map<String, Object> context = new HashMap<>();
        context.put("productData", data);
        
        String productName = "未知商品";
        String categoryName = "未知类目"; 
        
        if (data instanceof CilsProductSpu) {
            CilsProductSpu spu = (CilsProductSpu) data;
            productName = spu.getProductName();
            // SPU 对象里可能已经填充了 categoryName (在 Service 层)
            // 如果没有，尝试根据 ID 查字典
            if (spu.getCategoryName() != null) {
                categoryName = spu.getCategoryName();
            } else if (spu.getCategoryId() != null) {
                categoryName = dictDataService.selectDictLabel("cils_category", spu.getCategoryId().toString());
            }
        } else if (data instanceof CilsProductSku) {
            Long spuId = ((CilsProductSku) data).getSpuId();
            if (spuId != null) {
                CilsProductSpu spu = spuMapper.selectCilsProductSpuById(spuId);
                if (spu != null) {
                    productName = spu.getProductName();
                    // 关键修复：根据 SPU 的 categoryId 查出类目名称
                    if (spu.getCategoryId() != null) {
                        categoryName = dictDataService.selectDictLabel("cils_category", spu.getCategoryId().toString());
                    }
                }
            }
        }
        
        log.info("校验上下文: 商品名称={}, 类目={}", productName, categoryName);
        
        List<Map<String, String>> ruleList = new ArrayList<>();
        for (CilsRuleConfig rule : rules) {
            Map<String, String> ruleMap = new HashMap<>();
            String targetField = rule.getTargetField();

            if (data instanceof CilsProductSku) {
                if ("mainImage".equalsIgnoreCase(targetField)) {
                    targetField = "skuImage"; 
                }
                if ("productName".equalsIgnoreCase(targetField)) {
                    targetField = "specInfo"; 
                }
            }
            
            ruleMap.put("name", rule.getRuleName());
            ruleMap.put("field", targetField);
            ruleMap.put("content", rule.getAiPrompt());
            ruleMap.put("errorMsg", rule.getErrorMessage());
            ruleMap.put("productName", productName);
            ruleMap.put("categoryName", categoryName);
            
            String fieldType = targetField.toLowerCase();
            if (fieldType.contains("image") || fieldType.contains("img")) {
                ruleMap.put("type", "IMAGE");
            } else {
                ruleMap.put("type", "TEXT");
            }
            
            ruleList.add(ruleMap);
        }
        context.put("rules", ruleList);

        String jsonData = JSON.toJSONString(context);

        long startTime = System.currentTimeMillis();
        VerificationResult result = PythonRunnerUtils.runScript("py_scripts/universal_checker.py", jsonData);
        long costTime = System.currentTimeMillis() - startTime;
        
        log.info("<<< 校验引擎执行结束，耗时: {}ms，结果: {}", costTime, result.isPass() ? "PASS" : "BLOCK");
        
        return result;
    }
}
