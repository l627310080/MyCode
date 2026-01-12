import sys
import json
import base64
import requests
import os
from google import genai
from google.genai import types

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
        return f"API请求异常: {str(e)}"

def call_gemini_vision(image_data_base64, prompt):
    try:
        image_bytes = base64.b64decode(image_data_base64)

        response = client.models.generate_content(
            model="gemini-2.5-flash",
            contents=[
                types.Content(
                    parts=[
                        types.Part.from_text(text=prompt),
                        types.Part.from_bytes(data=image_bytes, mime_type="image/jpeg")
                    ]
                )
            ]
        )
        return response.text
    except Exception as e:
        return f"API请求异常: {str(e)}"

def download_image_as_base64(url):
    try:
        if url.startswith("/"):
            url = "http://localhost:8080" + url
        url = url.strip()
        response = requests.get(url, timeout=10)
        if response.status_code == 200:
            return base64.b64encode(response.content).decode('utf-8')
        return None
    except:
        return None

def check_image_ai(image_url_str, prompt_template, product_name):
    if not image_url_str: return True, ""

    image_urls = image_url_str.split(',')

    for image_url in image_urls:
        if not image_url.strip(): continue

        print(f"正在下载图片: {image_url} ...")
        base64_data = download_image_as_base64(image_url)

        if not base64_data:
            print(f"图片下载失败: {image_url}，跳过")
            continue

        # 升级 Prompt：加入图文一致性检查
        prompt = f"""
        你是一个严格的跨境电商合规审核员。
        商品标题是："{product_name}"。

        请检查这张图片：
        1. 是否包含武器、毒品、色情、暴力等违禁内容？
        2. **图片内容是否与商品标题一致？** (例如：标题是"显示器"，图片却是代码截图、风景或无关物体，视为违规)
        3. {prompt_template}

        如果图片合规且与标题一致，请只回复 "PASS"。
        如果有任何违规或不一致，请回复 "BLOCK:原因"。
        """

        print(f"开始请AI判断图片是否合法 (标题: {product_name}): {image_url}")
        result = call_gemini_vision(base64_data, prompt)
        print(f"AI回复: {result}")

        if "BLOCK" in result:
            return False, result.replace("BLOCK:", "").strip()
        elif "PASS" not in result:
            print(f"AI回复异常: {result}")

    return True, ""

def check_text_ai(text, prompt_template):
    if not text: return True, ""

    prompt = f"""
    {prompt_template}

    待审核文本："{text}"

    如果合规，请只回复 "PASS"。
    如果不合规，请回复 "BLOCK:原因"。
    """

    print(f"开始请AI判断文本是否合法: {text[:20]}...")
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
            rule_type = rule.get('type', 'TEXT')
            product_name = rule.get('productName', '未知商品') # 获取商品标题

            value = product.get(field)

            print(f"执行规则: {rule.get('name')}, 类型: {rule_type}, 字段: {field}")


            if rule_type == 'IMAGE':
                # 传入商品标题
                is_pass, reason = check_image_ai(str(value), content, product_name)
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
