import base64
import json
import os
import requests
import sys

from ai_client import AIClient

# 初始化 AI 客户端
client = AIClient()


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


def check_image_ai(image_url_str, prompt_template, rule_content, product_name, category_name):
    if not image_url_str: return True, ""

    image_urls = image_url_str.split(',')
    reasons = []

    for image_url in image_urls:
        if not image_url.strip(): continue

        print(f"正在下载图片: {image_url} ...")
        base64_data = download_image_as_base64(image_url)

        if not base64_data:
            print(f"图片下载失败: {image_url}，跳过")
            continue

        # 使用数据库传入的模板构建 Prompt
        # 替换占位符
        prompt = prompt_template.replace("{product_name}", product_name) \
            .replace("{category_name}", category_name) \
            .replace("{content}", rule_content)

        print(f"开始请AI判断图片是否合法 (标题: {product_name}, 类目: {category_name}): {image_url}")

        # 使用统一客户端调用 Vision 能力
        result = client.call_vision(base64_data, prompt)
        print(f"图片识别AI回复: {result}")

        if "BLOCK" in result:
            reasons.append(result.replace("BLOCK:", "").strip())
        elif "PASS" not in result:
            # 检查是否是 API 错误
            if "API_ERROR" in result or "OPENROUTER_ERROR" in result or "Error" in result:
                print(f"AI调用出错: {result}")
            else:
                print(f"AI回复异常: {result}")

    if reasons:
        return False, "; ".join(reasons)
    return True, ""


def check_text_ai(text, prompt_template, rule_content, category_name):
    if not text: return True, ""

    # 使用数据库传入的模板构建 Prompt
    prompt = prompt_template.replace("{category_name}", category_name) \
        .replace("{text}", text) \
        .replace("{content}", rule_content)

    # 使用统一客户端调用文本能力
    result = client.call_text(prompt)
    
    # 打印完整的 AI 响应日志，确保 Java 端能完整采集
    print(f"DEBUG: [文本识别结果] AI回复长度: {len(result)}")
    print(f"AI回复主体: {result}")

    if "BLOCK" in result:
        return False, result.replace("BLOCK:", "").strip()
    elif "PASS" not in result:
        print(f"AI回复异常: {result}")
        return True, ""

    return True, ""


def get_value_smart(product, field, rule_type):
    value = product.get(field)
    if value: return value

    if rule_type == 'IMAGE':
        return product.get('skuImage') or product.get('mainImage')
    elif rule_type == 'TEXT':
        return product.get('specInfo') or product.get('productName')

    return None


def main():
    try:
        raw_data = sys.stdin.buffer.read()
        json_data = raw_data.decode('utf-8', errors='replace')
        context = json.loads(json_data)
        product = context.get('productData', {})
        rules = context.get('rules', [])

        # 获取 Java 传来的 Prompt 模板
        tpl_image = context.get('tpl_image', '')
        tpl_text = context.get('tpl_text', '')

        # 如果没有传模板，使用默认兜底（防止旧代码报错，但应尽量避免）
        if not tpl_image:
            tpl_image = 'Check this image against product "{product_name}" (Category: {category_name}). Rule: {content}. Reply PASS or BLOCK:reason.'
        if not tpl_text:
            tpl_text = 'Check this text "{text}" (Category: {category_name}). Rule: {content}. Reply PASS or BLOCK:reason.'

        print(f"开始校验，规则数量: {len(rules)}")

        all_fail_reasons = []

        for rule in rules:
            field = rule.get('field')
            content = rule.get('content')  # 这是具体的规则内容，如"检查是否侵权"
            error_msg = rule.get('errorMsg', '校验失败')
            rule_type = rule.get('type', 'TEXT')

            product_name = rule.get('productName') or product.get('productName') or product.get('specInfo') or '未知商品'
            category_name = rule.get('categoryName') or '未知类目'

            value = get_value_smart(product, field, rule_type)

            print(f"执行规则: {rule.get('name')}, 类型: {rule_type}, 字段: {field}")

            is_pass = True
            reason = ""

            if rule_type == 'IMAGE':
                is_pass, reason = check_image_ai(str(value), tpl_image, content, product_name, category_name)
            else:
                is_pass, reason = check_text_ai(str(value), tpl_text, content, category_name)

            if not is_pass:
                full_reason = f"{error_msg}: {reason}"
                all_fail_reasons.append(full_reason)
                print(f"规则未通过: {full_reason}")

        if all_fail_reasons:
            final_reason = " | ".join(all_fail_reasons)
            print(f"FAIL_REASON:{final_reason}")
            print("BLOCK")
        else:
            print("校验通过")
            print("PASS")

    except Exception as e:
        print(f"FAIL_REASON:脚本执行异常 - {str(e)}")
        print("BLOCK")


if __name__ == "__main__":
    # 已经在 ai_client 中设置了 sys.stdout 的编码，这里不需要重复设置，或者保持一致
    # sys.stdout.reconfigure(encoding='utf-8')
    main()
