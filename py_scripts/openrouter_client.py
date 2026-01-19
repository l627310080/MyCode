import os
import requests
import json

class OpenRouterClient:
    """
    OpenRouter 通用 AI 调用客户端
    专注于网络请求、状态码判别和错误日志展示。
    """
    def __init__(self):
        # 自动从环境读取 Key
        self.api_key = os.environ.get("OPENROUTER_API_KEY")
        self.url = "https://openrouter.ai/api/v1/chat/completions"

    def chat(self, model, prompt, timeout=30):
        """
        核心调用方法
        :param model: 模型版本字符串
        :param prompt: 询问语句(或列表类型的复合内容)
        :return: AI回复的正文内容 或 格式化后的错误信息
        """
        # 1. 检查 Key
        if not self.api_key:
            return "OPENROUTER_ERROR: 401 message: ❌ 环境中缺少 OPENROUTER_API_KEY"

        # 2. 构造请求
        headers = {
            "Authorization": f"Bearer {self.api_key}",
            "Content-Type": "application/json"
        }
        
        # 兼容处理：如果 prompt 是列表，代表是 Vision 或复合内容
        messages_body = prompt if isinstance(prompt, list) else [{"role": "user", "content": prompt}]
        
        payload = {
            "model": model,
            "messages": messages_body
        }

        try:
            response = requests.post(self.url, headers=headers, json=payload, timeout=timeout)
            
            # 3. 状态码判别
            if response.status_code == 200:
                result = response.json()
                if 'choices' in result and len(result['choices']) > 0:
                    return result['choices'][0]['message']['content']
                return f"OPENROUTER_ERROR: 200 message: ❌ 返回内容为空 {json.dumps(result)}"
            
            elif response.status_code == 401:
                return f"OPENROUTER_ERROR: 401 message: ❌ 授权失败"
            
            elif response.status_code == 429:
                return "OPENROUTER_ERROR: 429 message: ❌ 频率超限"
            
            elif response.status_code in [500, 502, 503]:
                return f"OPENROUTER_ERROR: {response.status_code} message: ❌ 服务器繁忙"
            
            return f"OPENROUTER_ERROR: {response.status_code} message: ❌ 报错 {response.text}"

        except Exception as e:
            return f"OPENROUTER_ERROR: 999 message: 🌐 网络故障 {str(e)}"

# --- 执行自测 ---
if __name__ == "__main__":
    client = OpenRouterClient()
    # 简单的打招呼测试
    res = client.chat("google/gemma-3-27b-it:free", "你好，这是一次来自底层类恢复后的集成测试。")
    if "OPENROUTER_ERROR" in res:
        print(f"测试失败: {res}")
    else:
        print(f"测试成功! AI回复: {res}")
