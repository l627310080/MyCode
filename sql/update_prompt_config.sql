-- 插入 AI 校验相关的 Prompt 模板到 cils_rule_config 表
-- 使用 target_object='TEMPLATE' 来标识这是系统模板
-- 使用 target_field 来标识模板类型

INSERT INTO cils_rule_config (rule_name, target_object, target_field, ai_prompt, error_message, is_active, create_by, create_time, rule_type)
VALUES
('AI图片校验通用模板', 'TEMPLATE', 'checker_image', '你是一个严格的跨境电商合规审核员。\n商品标题是："{product_name}"。\n商品类目是："{category_name}"。\n\n请检查这张图片：\n1. 是否包含武器、毒品、色情、暴力等违禁内容？\n2. **图片内容是否与商品标题及类目一致？** \n   (例如：类目是"手机"，但图片是"衣服"，必须拦截)\n3. {content}\n\n如果图片合规且一致，请只回复 "PASS"。\n如果有任何违规或不一致，请回复 "BLOCK:原因"。', '系统模板', 1, 'admin', sysdate(), 'TEXT'),

('AI文本校验通用模板', 'TEMPLATE', 'checker_text', '{content}\n\n商品类目是："{category_name}"。\n待审核文本："{text}"\n\n请检查文本是否与类目一致？\n\n如果合规，请只回复 "PASS"。\n如果不合规，请回复 "BLOCK:原因"。', '系统模板', 1, 'admin', sysdate(), 'TEXT'),

('AI翻译通用模板', 'TEMPLATE', 'translator', '你是一个专业的跨境电商翻译专家。\n请将以下商品信息翻译成目标语言。\n\n{content}', '系统模板', 1, 'admin', sysdate(), 'TEXT');
