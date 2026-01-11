CILS - 跨境电商智能商品中台 (Cross-border Intelligent Listing System)
📖 项目简介
CILS 是一个基于 Spring Boot 3 + Vue 3 的现代化跨境电商核心业务中台。本项目以 RuoYi-Vue
脚手架为基础框架，针对跨境电商业务场景进行了深度的定制开发与架构升级。
系统致力于解决跨境卖家在 商品合规清洗、多平台一键铺货 以及 高并发库存管理 方面的痛点。它不仅仅是一个 CRUD 系统，更是一个融合了
AI 智能审核 (Google Gemini)、分布式中间件 (Redis/Kafka) 和 复杂业务策略 的企业级解决方案雏形。
✨ 核心功能亮点

1. 🤖 AI 驱动的合规校验引擎
   •
   多模态审核：集成 Google Gemini Pro Vision，同时支持对商品标题（文本）和主图（视觉）进行深度扫描。
   •
   智能拦截：自动识别违禁品（武器/毒品）、侵权品牌、色情暴力内容及非目标国语言字符。
   •
   架构解耦：采用 Java 调度 + Python 执行 的富上下文 (Rich Context) 模式，通过标准输入流 (Stdin) 实现跨语言无损通信。
2. 🚀 一键铺货与动态定价
   •
   自动 Mapping：在录入 SKU 库存的同时，支持动态选择发布目标（如 Amazon US, Shopee TH）。
   •
   智能定价：内置汇率服务 (ExchangeRateService)，基于 采购价 * 实时汇率 * 定价系数 自动计算目标市场售价。
   •
   策略推送：基于 策略模式 (Strategy Pattern) 实现多平台推送逻辑的动态分发。
3. ⚡ 高并发库存管理
   •
   防超卖架构：采用 Redis 原子扣减 (Lua/DecrBy) 应对秒杀级流量，确保库存扣减的强一致性。
   •
   削峰填谷：引入 Kafka 消息队列，将数据库写入操作异步化，保护核心数据库不被击穿。
   •
   全渠道同步：本地库存变动后，自动触发反向同步机制，实时更新外部平台库存。
4. 🌲 完善的商品体系
   •
   SPU/SKU 分离：标准的电商商品模型，支持多规格变体。
   •
   无限层级类目：基于 treeselect 实现的无限层级商品类目管理。
   •
   交互优化：前端集成远程搜索、级联选择等高级交互组件。

   🛠 技术栈

| 类别                | 技术                 | 说明       |
|-------------------|--------------------|----------| 
| 后端                | Spring Boot 3      | 核心框架     | 
| MyBatis-Plus      | ORM 框架             |          |
| Spring Kafka      | 消息队列集成             |          |
| Spring Retry      | 外部调用重试机制           |          |
| 前端                | Vue 3 + Element UI | 基础 UI 框架 | 
| Vue Treeselect    | 树形选择组件             |          |
| 中间件               | MySQL 8            | 持久化存储    | 
| Redis             | 缓存 & 原子计数器         |          | 
| Kafka             | 流量削峰 & 异步解耦        |          | 
| AI & 脚本           | Python 3           | 脚本执行环境   |
| Google Gemini API | AI 视觉与文本模型         |          |

📂 项目结构说明


john-cils  核心业务模块 
├── domain  实体类  CilsProductSpu, CilsProductSku, CilsRuleConfig 
├── controller  控制层  CilsProductSpuController (含远程搜索), CilsPlatformMappingController 
├── service  业务逻辑层 
│ ├── impl  实现类  CilsProductSkuServiceImpl (含一键铺货逻辑) 
│ └── push  推送策略  PlatformPushService, AmazonPushStrategy 
├── verification  校验引擎
│ ├── async  异步调度  AsyncVerificationService (@Async 线程池) 
│ ├── engine  核心引擎  VerificationEngine (构造富上下文) 
│ └── domain  校验实体  Verifiable (接口), VerificationResult 
├── utils  工具类  PythonRunnerUtils (Stdin调用), AmazonPushUtils 
└── mq  消息队列  StockConsumer (库存落地消费者) 