import os
import json
import requests
import base64
import sys
import io
import time
from google import genai
from google.genai import types

from openrouter_client import OpenRouterClient
from deepseek_client import DeepSeekClient
from openai_client import OpenAIClient

# 强制设置标准输出和标准错误为 UTF-8
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
sys.stderr = io.TextIOWrapper(sys.stderr.buffer, encoding='utf-8')

class AIClient:
    def __init__(self):
        # 1. 自动从系统环境变量获取配置
        self.google_api_key = os.environ.get("GEMINI_API_KEY")
        self.deepseek_api_key = os.environ.get("DEEPSEEK_API_KEY")
        self.openai_api_key = os.environ.get("OPENAI_API_KEY")
        
        # 2. 实例化底层客户端
        self.or_client = OpenRouterClient()
        self.ds_client = DeepSeekClient()
        self.oa_client = OpenAIClient()

        # 3. 初始化 Gemini (如果 KEY 可用)
        self.gemini_client = None
        if self.google_api_key:
            try:
                self.gemini_client = genai.Client(api_key=self.google_api_key)
            except Exception as e:
                print(f"Warning: Gemini 初始化失败: {e}", flush=True)

    def call_text(self, prompt, model="gemini-2.5-flash"):
        """文本接口：Gemini -> OpenRouter -> DeepSeek (带重试)"""
        # --- 演示专场：如果检测到演示开关，强制跳过 Gemini 以观察降级 ---
        if os.environ.get("AI_DEMO_FAIL_GEMINI") == "true":
            print(">>>> [演示] 模拟 Gemini 服务不可用，准备启动降级链路...", flush=True)
        elif self.gemini_client:
            print(f"DEBUG: [1/3] >>> 正在发起对 [Gemini] 的文本调用 (Model: {model})...", flush=True)
            try:
                response = self.gemini_client.models.generate_content(model=model, contents=prompt)
                result = response.text
                print(f"DEBUG: <<< [Gemini] 返回内容: {result}", flush=True)
                return result
            except Exception as e:
                print(f"DEBUG: ❌ [Gemini] 接口报错: {e}", flush=True)
                print("DEBUG: >>> 准备切换至下一顺位 (OpenRouter)...", flush=True)
        else:
            print("DEBUG: ⚠️ [Gemini] 未初始化，跳过。", flush=True)
        
        # --- 第二顺位：OpenRouter 轮询 ---
        result = self._call_with_fallback(prompt)
        
        # 如果 OpenRouter 也没有拿到结果，则进入 DeepSeek 终极重试
        if "API_ERROR" in result or "OPENROUTER_ERROR" in result:
             print("DEBUG: >>> OpenRouter 均宣告失败，启动 DeepSeek 增强重试模式...", flush=True)
             return self._call_deepseek_with_retry(prompt)
        
        return result

    def _call_deepseek_with_retry(self, prompt, max_retries=3, delay=5):
        """DeepSeek 专线：带 5s 间隔的 3 次重试逻辑"""
        for i in range(max_retries):
            print(f"DEBUG: [3/3] >>> 正在发起 DeepSeek 最终保底 (尝试 {i+1}/{max_retries})...", flush=True)
            messages = [{"role": "user", "content": prompt}]
            success, result = self.ds_client.chat(messages=messages)
            
            if success:
                print(f"DEBUG: <<< [DeepSeek] 最终返回结果: {result}", flush=True)
                return result
            
            print(f"DEBUG: ⚠️ DeepSeek 第 {i+1} 次失败: {result}", flush=True)
            if i < max_retries - 1:
                print(f"DEBUG: ⏳ 等待 {delay} 秒后重试...", flush=True)
                time.sleep(delay)

        return f"API_ERROR: 全链路文本模型耗尽 (Gemini/OpenRouter/DeepSeek)。最后一次报错: {result}"

    def call_vision(self, image_base64, prompt, model="gemini-2.5-flash"):
        """视觉接口：Gemini -> OpenAI (精准降级)"""
        # --- 演示专场：视觉降级模拟 ---
        if os.environ.get("AI_DEMO_FAIL_GEMINI") == "true":
            print(">>>> [演示] 模拟 Gemini 视觉识别超时，切换至 OpenAI 专线...", flush=True)
        elif self.gemini_client:
            print(f"DEBUG: [1/2] >>> 正在发起对 [Gemini] 的视觉调用 (Model: {model})...", flush=True)
            try:
                image_bytes = base64.b64decode(image_base64)
                response = self.gemini_client.models.generate_content(
                    model=model,
                    contents=[
                        types.Content(
                            parts=[
                                types.Part.from_text(text=prompt),
                                types.Part.from_bytes(data=image_bytes, mime_type="image/jpeg")
                            ]
                        )
                    ]
                )
                result = response.text
                print(f"DEBUG: <<< [Gemini] 视觉识别结果: {result}", flush=True)
                return result
            except Exception as e:
                print(f"DEBUG: ❌ [Gemini] 视觉接口报错: {e}", flush=True)
                print("DEBUG: >>> 准备切换至 OpenAI 视觉专线...", flush=True)
        else:
            print("DEBUG: ⚠️ [Gemini] 未初始化，跳过。", flush=True)
        
        # --- 第二顺位：OpenAI 官方视觉专线 ---
        last_error = "未启动 OpenAI"
        if self.openai_api_key:
            print(f"DEBUG: [2/2] >>> 正在发起对 [OpenAI] 的视觉调用 (Model: gpt-4o-mini)...", flush=True)
            messages = [
                {
                    "role": "user",
                    "content": [
                        {"type": "text", "text": prompt},
                        {"type": "image_url", "image_url": {"url": f"data:image/jpeg;base64,{image_base64}"}}
                    ]
                }
            ]
            success, result = self.oa_client.chat(model="gpt-4o-mini", messages=messages)
            if success:
                print(f"DEBUG: <<< [OpenAI] 视觉识别成功: {result}", flush=True)
                return result
            last_error = result
            print(f"DEBUG: ❌ [OpenAI] 视觉接口最终报错: {last_error}", flush=True)
        else:
            print("DEBUG: ⚠️ [OpenAI] 未配置 OPENAI_API_KEY，跳过视觉专线。请检查 .env 文件。", flush=True)

        return f"API_ERROR: 全链路视觉模型耗尽 (Gemini/OpenAI)。最后结果: {last_error}"

    def _call_with_fallback(self, prompt, image_base64=None):
        """
        全能降级决策引擎
        """
        # --- 第二顺位：OpenRouter 轮询链项 ---
        # 优先选择具备视觉能力的免费模型，防止降级后无法看图
        fallback_models = [
            "meta-llama/llama-3.3-70b-instruct:free", # 视觉专精
            "qwen/qwen-2.5-vl-7b-instruct:free", # 视觉强项
            "google/gemma-3-27b-it:free", # 通用
        ]

        print(f"DEBUG: [2/3] 开始轮询 OpenRouter 免费集群以寻找替代模型...", flush=True)

        for i, model_name in enumerate(fallback_models):
            print(f"DEBUG: >>> [{i+1}/{len(fallback_models)}] 正在尝试 OpenRouter 模型: {model_name}...", flush=True)
            
            # 针对 OpenRouter 免费模型的 Prompt 强化：防止废话
            strict_prompt = f"{prompt}\n\nIMPORTANT: Respond ONLY with the required format (e.g., PASS or BLOCK:reason). No preamble, no greetings."
            
            # --- 智能路由逻辑：如果是视觉请求，跳过纯文本模型 ---
            is_vision_model = any(kw in model_name.lower() for kw in ["vision", "vl", "gemini", "gemma"])
            
            input_content = strict_prompt
            if image_base64:
                 if is_vision_model:
                     input_content = [
                        {"type": "text", "text": strict_prompt},
                        {"type": "image_url", "image_url": {"url": f"data:image/jpeg;base64,{image_base64}"}}
                     ]
                 else:
                     print(f"DEBUG: ⏭️ 跳过不支持视觉的模型 {model_name}，寻找下一顺位...", flush=True)
                     continue # 关键修复：直接跳过，不要把图片请求发给纯文本模型导致误报

            result = self.or_client.chat(model_name, input_content)
            
            # 去除两端空格和多余换行
            result = result.strip()
            print(f"DEBUG: <<< [{model_name}] 返回内容: {result}", flush=True)
            
            if "OPENROUTER_ERROR" in result:
                if any(kw in result for kw in ["429", "500", "502", "503", "999"]):
                    print(f"DEBUG: ⚠️ {model_name} 暂时不可用，准备自动切换下一模型...", flush=True)
                    continue
                else:
                    return result # 401 等错误不重试
            
            return result

            print(f"DEBUG: ⚠️ OpenRouter 轮询完毕，未获得有效结果。", flush=True)

        return "API_ERROR: OpenRouter 链条耗尽。"
