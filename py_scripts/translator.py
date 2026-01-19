import sys
import json
import os
from ai_client import AIClient

# 初始化 AI 客户端
client = AIClient()

def main():
    try:
        raw_data = sys.stdin.buffer.read()
        json_data = raw_data.decode('utf-8', errors='replace')
        params = json.loads(json_data)
        
        # 这里的 prompt 实际上是待翻译的内容 + 目标语言要求
        # 我们假设 Java 端传来的 'prompt' 字段包含了这些信息
        # 或者，如果 Java 端改了，传的是 'content' 和 'template'
        
        content = params.get('content') # 待翻译内容
        template = params.get('template') # 翻译模板
        
        # 兼容旧逻辑：如果只传了 prompt，则直接使用
        full_prompt = params.get('prompt')
        
        if content and template:
            full_prompt = template.replace("{content}", content)
        
        if not full_prompt:
            print("FAIL_REASON:Prompt为空")
            print("BLOCK")
            return

        # 使用统一的 AI 客户端调用
        result = client.call_text(full_prompt)
        
        # 打印自愈后的结果概况
        print(f"DEBUG: [翻译请求结果] AI回复长度: {len(result)}")
        
        # 错误检查：如果结果包含错误关键字，走报错流程
        if "API_ERROR" in result or "OPENROUTER_ERROR" in result:
            print(f"FAIL_REASON:{result}")
            print("BLOCK")
            return

        # 成功，直接输出翻译结果作为 message
        # 清理结果中的换行符
        result = result.strip()
        print(f"FAIL_REASON:{result}") 
        print("PASS")
        
    except Exception as e:
        print(f"FAIL_REASON:脚本执行异常 - {str(e)}")
        print("BLOCK")

if __name__ == "__main__":
    # sys.stdout.reconfigure(encoding='utf-8')
    main()
