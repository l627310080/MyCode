package com.john.cils.service;

import com.alibaba.fastjson2.JSON;
import com.john.cils.utils.python.PythonRunnerUtils;
import com.john.cils.verification.domain.VerificationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * AI 翻译服务
 *
 * @author john
 * @date 2026-01-14
 */
@Service
public class AiTranslationService {

    private static final Logger log = LoggerFactory.getLogger(AiTranslationService.class);

    /**
     * 调用 AI 进行文本翻译
     *
     * @param text           待翻译文本
     * @param targetLanguage 目标语言 (如 "English", "Thai")
     * @return 翻译后的文本
     */
    public String translate(String text, String targetLanguage) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        log.info(">>> 开始 AI 翻译: '{}' -> {}", text, targetLanguage);

        String prompt = String.format(
                "你是一位精通跨境电商的专业翻译。请将以下商品文本翻译成 **%s**。\n" +
                "风格要求：地道、吸引人、符合当地电商习惯。\n" +
                "仅返回翻译后的文本，不要包含任何解释、标点符号外的其他内容。\n" +
                "待翻译文本：\"%s\"",
                targetLanguage, text
        );

        Map<String, String> params = new HashMap<>();
        params.put("text", text);
        params.put("targetLanguage", targetLanguage);
        params.put("prompt", prompt);
        
        String jsonData = JSON.toJSONString(params);
        
        VerificationResult result = PythonRunnerUtils.runScript("py_scripts/translator.py", jsonData);
        
        if (result.isPass()) {
            String translatedText = result.getMessage(); 
            log.info("<<< 翻译完成: {}", translatedText);
            return translatedText;
        } else {
            log.error("翻译失败: {}", result.getFailReason());
            // 关键修改：在返回值中明确标识翻译失败，方便排查
            return "[翻译失败] " + text;
        }
    }
}
