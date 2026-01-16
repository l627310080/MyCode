import sys
import json
import os
from google import genai

# 从环境变量获取 API Key
client = genai.Client()

def call_gemini_text(prompt):
    try:
        response = client.models.generate_content(
            model="gemini-2.5-flash", 
            contents=prompt
        )
        return response.text
    except Exception as e:
        return f"API_ERROR: {str(e)}"

def main():
    try:
        raw_data = sys.stdin.buffer.read()
        json_data = raw_data.decode('utf-8', errors='replace')
        params = json.loads(json_data)
        
        prompt = params.get('prompt')
        
        if not prompt:
            print("FAIL_REASON:Prompt为空")
            print("BLOCK")
            return

        result = call_gemini_text(prompt)
        
        if "API_ERROR" in result:
            print(f"FAIL_REASON:{result}")
            print("BLOCK")
        else:
            # 成功，直接输出翻译结果作为 message
            # 注意：VerificationResult 会把 FAIL_REASON 后的内容作为 message
            # 这里我们稍微 hack 一下，把翻译结果打印在 FAIL_REASON 后面，但状态打印 PASS
            # Java 端需要适配：如果 PASS，则 message 是翻译结果
            
            # 清理结果中的换行符
            result = result.strip()
            print(f"FAIL_REASON:{result}") 
            print("PASS")
        
    except Exception as e:
        print(f"FAIL_REASON:脚本执行异常 - {str(e)}")
        print("BLOCK")

if __name__ == "__main__":
    sys.stdout.reconfigure(encoding='utf-8')
    main()
