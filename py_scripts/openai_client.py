import os
import base64
try:
    from openai import OpenAI
except ImportError:
    # 自动尝试兼容旧版或未安装 SDK 的情况，但优先遵循用户模板
    OpenAI = None

class OpenAIClient:
    """
    OpenAI 官方 SDK 客户端 (2025 模板版)
    自适应支持文本与视觉请求。
    """
    def __init__(self):
        self.api_key = os.environ.get("OPENAI_API_KEY")
        if not self.api_key:
            self.client = None
        else:
            # 默认会读取 OPENAI_API_KEY 环境变量
            self.client = OpenAI()

    def chat(self, model="gpt-4o-mini", messages=None, timeout=30):
        """
        发起对话请求
        :param model: 模型名称 (图片识别最低版本建议使用 gpt-4o-mini)
        :param messages: 消息列表
        :return: (成功标志, AI回复内容或错误信息)
        """
        if not self.client:
            return False, "OPENAI_ERROR: 401 message: ❌ 环境中缺少 OPENAI_API_KEY 或 SDK 未安装"

        try:
            # 转换消息格式以适配 2025 新版 responses.create 的严格结构
            # 官网最新规范：视觉项类型为 input_text 和 input_image
            if len(messages) > 0:
                raw_content = messages[-1].get("content")
                
                if isinstance(raw_content, list):
                    # 视觉/复合请求：转换格式
                    formatted_content = []
                    for item in raw_content:
                        if item.get("type") == "text":
                            formatted_content.append({
                                "type": "input_text",
                                "text": item.get("text")
                            })
                        elif item.get("type") == "image_url":
                            image_data = item.get("image_url", {}).get("url", "")
                            # 官方规范：Base64 必须以 data:image/jpeg;base64, 开头
                            if image_data and not image_data.startswith("data:"):
                                image_data = f"data:image/jpeg;base64,{image_data}"
                            
                            formatted_content.append({
                                "type": "input_image",
                                "image_url": image_data
                            })
                    
                    input_data = [
                        {
                            "role": "user",
                            "content": formatted_content
                        }
                    ]
                else:
                    # 纯文本请求
                    input_data = [
                        {
                            "role": "user",
                            "content": [
                                {
                                    "type": "input_text",
                                    "text": str(raw_content)
                                }
                            ]
                        }
                    ]
            else:
                input_data = []

            # 调用新版接口
            response = self.client.responses.create(
                model=model,
                input=input_data
            )
            
            # 使用用户指定的 output_text 属性获取结果
            result = response.output_text
            
            if result:
                return True, result
            return False, "OPENAI_ERROR: 200 message: ❌ AI 返回了空内容"

        except Exception as e:
            # 自动处理限流 (429) 或其他 SDK 异常
            err_msg = str(e)
            if "429" in err_msg:
                return False, f"OPENAI_ERROR: 429 message: ❌ 额度耗尽或频率超限: {err_msg}"
            return False, f"OPENAI_ERROR: 500 message: ❌ SDK 调用失败: {err_msg}"

# --- 测试代码 ---
if __name__ == "__main__":
    client = OpenAIClient()
    # 纯文本测试
    # success, res = client.chat(model="gpt-5-nano", messages=[{"role": "user", "content": "你好"}])
    # print(res if success else f"失败: {res}")
