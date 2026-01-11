import sys
import json
import base64
import requests

# 配置你的 Gemini API Key
API_KEY = "YOUR_API_KEY"
TEXT_API_URL = f"https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key={API_KEY}"
VISION_API_URL = f"https://generativelanguage.googleapis.com/v1beta/models/gemini-pro-vision:generateContent?key={API_KEY}"

def call_gemini_text(prompt):
    headers = {'Content-Type': 'application/json'}
    data = { "contents": [{ "parts": [{"text": prompt}] }] }
    try:
        response = requests.post(TEXT_API_URL, headers=headers, json=data, timeout=10)
        if response.status_code == 200:
            result = response.json()
            try:
                return result['candidates'][0]['content']['parts'][0]['text']
            except:
                return "API解析失败"
        return f"API调用失败: {response.status_code}"
    except Exception as e:
        return f"API请求异常: {str(e)}"

def call_gemini_vision(image_data_base64, prompt):
    headers = {'Content-Type': 'application/json'}
    data = {
        "contents": [{
            "parts": [
                {"text": prompt},
                { "inline_data": { "mime_type": "image/jpeg", "data": image_data_base64 } }
            ]
        }]
    }
    try:
        response = requests.post(VISION_API_URL, headers=headers, json=data, timeout=30)
        if response.status_code == 200:
            result = response.json()
            try:
                return result['candidates'][0]['content']['parts'][0]['text']
            except:
                return "API解析失败"
        return f"API调用失败: {response.status_code}"
    except Exception as e:
        return f"API请求异常: {str(e)}"

def download_image_as_base64(url):
    try:
        if url.startswith("/"):
            url = "http://localhost:8080" + url
        response = requests.get(url, timeout=10)
        if response.status_code == 200:
            return base64.b64encode(response.content).decode('utf-8')
        return None
    except:
        return None

def check_image_ai(image_url, prompt_template):
    if not image_url: return True, ""
    print(f"正在下载图片: {image_url} ...")
    base64_data = download_image_as_base64(image_url)
    if not base64_data: return True, "图片下载失败，跳过检查"

    prompt = f"""
    {prompt_template}

    如果图片合规，请只回复 "PASS"。
    如果有任何违规，请回复 "BLOCK:原因"。
    """
    result = call_gemini_vision(base64_data, prompt)
    print(f"AI回复: {result}")

    if "PASS" in result: return True, ""
    elif "BLOCK" in result: return False, result.replace("BLOCK:", "").strip()
    else: return True, f"AI回复异常: {result}"

def check_text_ai(text, prompt_template):
    if not text: return True, ""

    prompt = f"""
    {prompt_template}

    待审核文本："{text}"

    如果合规，请只回复 "PASS"。
    如果不合规，请回复 "BLOCK:原因"。
    """
    result = call_gemini_text(prompt)
    print(f"AI回复: {result}")

    if "PASS" in result: return True, ""
    elif "BLOCK" in result: return False, result.replace("BLOCK:", "").strip()
    else: return True, f"AI回复异常: {result}"

def main():
    try:
        raw_data = sys.stdin.buffer.read()
        json_data = raw_data.decode('utf-8', errors='replace')
        context = json.loads(json_data)
        product = context.get('productData', {})
        rules = context.get('rules', [])

        print(f"开始校验，规则数量: {len(rules)}")

        for rule in rules:
            field = rule.get('field')
            content = rule.get('content')
            error_msg = rule.get('errorMsg', '校验失败')
            rule_type = rule.get('type', 'TEXT') # 获取 Java 传来的类型

            value = product.get(field)

            print(f"执行规则: {rule.get('name')}, 类型: {rule_type}, 字段: {field}")

            is_pass = True
            reason = ""

            if rule_type == 'IMAGE':
                is_pass, reason = check_image_ai(str(value), content)
            else:
                is_pass, reason = check_text_ai(str(value), content)

            if not is_pass:
                print(f"FAIL_REASON:{error_msg} - {reason}")
                print("BLOCK")
                return

        print("校验通过")
        print("PASS")

    except Exception as e:
        print(f"FAIL_REASON:脚本执行异常 - {str(e)}")
        print("BLOCK")

if __name__ == "__main__":
    sys.stdout.reconfigure(encoding='utf-8')
    main()
