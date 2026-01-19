import os
import requests
import json

class DeepSeekClient:
    """
    DeepSeek API 通用客户端
    封装了基础的请求逻辑、错误处理和超时管理。
    """
    def __init__(self):
        # 自动从环境读取 Key
        self.api_key = os.environ.get("DEEPSEEK_API_KEY")
        self.url = "https://api.deepseek.com/chat/completions"

    def chat(self, model="deepseek-chat", messages=None, timeout=30):
        """
        发起对话请求
        :param model: 模型名称 (默认 deepseek-chat)
        :param messages: 消息列表
        :param timeout: 超时时间
        :return: (成功标志, AI回复内容或错误信息)
        """
        if not self.api_key:
            return False, "DEEPSEEK_ERROR: 401 message: ❌ 环境中缺少 DEEPSEEK_API_KEY"

        if not messages:
            return False, "DEEPSEEK_ERROR: 400 message: ❌ 消息内容为空"

        headers = {
            "Authorization": f"Bearer {self.api_key}",
            "Content-Type": "application/json"
        }
        
        payload = {
            "model": model,
            "messages": messages,
            "stream": False
        }

        try:
            response = requests.post(self.url, headers=headers, json=payload, timeout=timeout)
            
            if response.status_code == 200:
                result = response.json()
                if 'choices' in result and len(result['choices']) > 0:
                    content = result['choices'][0]['message']['content']
                    return True, content
                return False, f"DEEPSEEK_ERROR: 200 message: ❌ 返回内容格式异常"
            
            elif response.status_code == 401:
                return False, f"DEEPSEEK_ERROR: 401 message: ❌ 授权失败/Key无效"
            
            elif response.status_code == 429:
                return False, "DEEPSEEK_ERROR: 429 message: ❌ 频率超限 (Quota Exceeded)"
            
            elif response.status_code in [500, 502, 503]:
                return False, f"DEEPSEEK_ERROR: {response.status_code} message: ❌ 服务器繁忙/崩溃"
            
            else:
                return False, f"DEEPSEEK_ERROR: {response.status_code} message: ❌ 报错 {response.text}"

        except Exception as e:
            return False, f"DEEPSEEK_ERROR: 999 message: 🌐 网络故障 {str(e)}"

# --- 测试代码 ---
if __name__ == "__main__":
    client = DeepSeekClient()
    success, res = client.chat(messages=[{"role": "user", "content": "你好，DeepSeek测试。"}])
    print(res if success else f"失败: {res}")
