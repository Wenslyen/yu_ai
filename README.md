# yu_ai · Java 工程师转型 AI Agent 学习项目

> 一个面向**传统 Java/Spring 工程师**的、循序渐进的 AI Agent 学习项目。
> 用主流框架 **Spring AI**（Java 生态最主流的 AI 框架，类比 LangChain）
> 把 LLM 应用从「调一次接口」一路推进到「能用工具、会检索、有记忆、可编排的智能体」。

---

## 1. 项目目标

作为长期写 Spring Boot 业务代码的 Java 工程师，要转型 Agent 开发，最大的鸿沟不是语言，而是 **思维范式的切换**：

| 传统 Java 思维                  | Agent 思维                                                |
| ------------------------------- | --------------------------------------------------------- |
| 业务规则写死在代码里            | 业务规则交给 LLM 推理，代码只提供工具                     |
| Controller → Service → DAO      | Prompt → Memory → Tools → VectorStore → Output            |
| 异常用 try/catch 处理           | 模型不确定性用 prompt 约束 / 重试 / 校验输出              |
| 性能靠 SQL 优化                 | 性能靠 Prompt 缩短、缓存、流式输出、模型选择              |
| 测试用 JUnit + Mock             | 还要做 **eval（评测集）**：相同输入 → 期望响应是否合格    |

本项目用 **8 个由浅入深的模块** 让你一边写代码，一边把思维切过去。

---

## 2. 技术选型

| 维度        | 选型                                            | 备注                                                                |
| ----------- | ----------------------------------------------- | ------------------------------------------------------------------- |
| 语言/构建   | Java 17 + Maven                                 | Spring Boot 3.x 标配                                                |
| 框架        | **Spring Boot 3.4 + Spring AI 1.0.0-M5**        | Java 生态最主流 AI 框架，对 Spring 用户零学习成本                   |
| 模型 (Chat) | DeepSeek（OpenAI 兼容）                         | 国内可用、便宜、效果不错；切其它模型只改 `application.yml`          |
| Embedding   | 本地 Transformers (ONNX, sentence-transformers) | RAG 用，不依赖外部 embedding API，开箱即用                          |
| 向量库      | `SimpleVectorStore`（内存）                     | 学习用最轻量；生产可换 PgVector / Milvus / Redis Stack              |
| Web         | Spring MVC + WebFlux（流式）                    | 演示同步与 SSE 流式两种返回                                         |

> 为什么用 Spring AI 而不是 LangChain4j？两者都很好，Spring AI 与 Spring Boot 集成最深、文档最齐、Advisor 体系直接对应「Agent 拦截链」概念，更适合 Spring 老兵入门。学完本项目，再迁到 LangChain4j 几乎是无缝的。

---

## 3. 总体架构

```
                ┌───────────────────────┐
                │   HTTP / SSE 入口     │  Controller 层（每个模块一个 Controller）
                └──────────┬────────────┘
                           ▼
   ┌────────────────────────────────────────────────────────┐
   │               ChatClient（核心门面）                    │
   │   prompt() → advisors() → functions() → call/stream()  │
   └──────┬───────────┬────────────┬───────────┬────────────┘
          ▼           ▼            ▼           ▼
     ┌────────┐  ┌─────────┐  ┌────────┐  ┌──────────┐
     │ Memory │  │  Tools  │  │  RAG   │  │ Output   │
     │Advisor │  │Function │  │ QA Adv │  │Converter │
     └────────┘  └─────────┘  └────────┘  └──────────┘
          ▲           ▲            ▲           ▲
          │           │            │           │
   ChatMemory     @Bean Function  VectorStore  POJO/JSON
                                  Embedding
```

> **一句话理解 Spring AI**：`ChatClient` 是门面，相当于 `RestTemplate`；`Advisor` 是拦截器链，做记忆、检索、日志这些横切关注点；`Function/Tool` 是给 LLM 调用的「方法」；`VectorStore + EmbeddingModel` 是 RAG 基础设施。

---

## 4. 目录结构与模块作用

```
yu_ai/
├── pom.xml                              # Maven 依赖
├── README.md                            # 本文档
├── src/main/java/qieyu/yu_ai/
│   ├── YuAiApplication.java             # 启动入口
│   ├── config/                          # ChatClient 与 ChatMemory 全局 Bean
│   │   ├── ChatClientConfig.java
│   │   └── ChatMemoryConfig.java
│   ├── module01_chat/                   # 模块 1：基础对话（同步 + 流式）
│   ├── module02_prompt/                 # 模块 2：Prompt 工程
│   ├── module03_memory/                 # 模块 3：多轮会话记忆
│   ├── module04_structured/             # 模块 4：结构化输出（LLM → POJO）
│   ├── module05_tools/                  # 模块 5：Function Calling（工具调用）
│   ├── module06_rag/                    # 模块 6：RAG 检索增强
│   ├── module07_agent/                  # 模块 7：ReAct Agent（自主智能体）
│   ├── module08_workflow/               # 模块 8：Workflow 编排（链/并行/路由）
│   └── controller/                      # 旧版 demo 保留（基础 Controller 例子）
├── src/main/resources/
│   ├── application.yml
│   ├── prompts/                         # Prompt 模板（与代码解耦）
│   ├── docs/                            # RAG 知识库样例文档
│   └── static/index.html                # 各模块入口导航
```

### 4.1 各模块定位与学到什么

| #   | 模块            | 一句话定位                                            | 你会学到                                                                                                          |
| --- | --------------- | ----------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------- |
| 1   | **chat**        | 「Hello LLM」                                         | `ChatClient` 同步 / 流式（SSE）调用，与 `RestTemplate` 类比                                                       |
| 2   | **prompt**      | 把请求「写得让模型听话」                              | System / User Role、`PromptTemplate` 占位符、外部 `.st` 模板文件                                                  |
| 3   | **memory**      | 给无状态的 LLM 加上「上下文」                         | `ChatMemory`、`MessageChatMemoryAdvisor`、按 `conversationId` 隔离会话                                            |
| 4   | **structured**  | 让 LLM 直接吐 JSON / POJO                             | `BeanOutputConverter`，把响应映射为 `MovieReview` 这种业务对象，**这是和后端业务代码打通的关键**                  |
| 5   | **tools**       | 让 LLM 调用你的 Java 方法                             | Function Calling：定义 `@Bean Function<Req,Resp>` + `@Description`，`functions("xxx")` 注入                       |
| 6   | **rag**         | 让 LLM 「带着资料」回答                               | EmbeddingModel、`SimpleVectorStore`、`QuestionAnswerAdvisor`、文档切分                                            |
| 7   | **agent**       | LLM 自主选择「思考-行动-观察」循环                    | ReAct 模式（Reason + Act），用 prompt + tools 自己组合一个最小 Agent                                              |
| 8   | **workflow**    | 多步骤、多角色协同                                    | Chain（串行）/ Parallel（并行）/ Routing（路由）三种典型编排，对应 Anthropic 官方"building effective agents"     |

> 学习顺序就是**模块顺序**，每模块都能独立运行，互不依赖，递进关系如下：
>
> 1 基础调用 → 2 学会写 prompt → 3 加上记忆 → 4 输出能被业务代码消费的结构 → 5 让模型反过来调用业务代码 → 6 把企业知识接入 → 7+8 把以上能力组合成 Agent。

---

## 5. 快速开始

### 5.1 拉依赖
```bash
cd yu_ai
./mvnw -DskipTests package
```
（如果没有 mvnw，用本机 `mvn` 也行）

### 5.2 配置 API Key

把 `application.yml` 里的 key **改成自己的**或注入环境变量：
```yaml
spring:
  ai:
    openai:
      api-key: ${DEEPSEEK_API_KEY:你的key}
      base-url: https://api.deepseek.com
```
推荐用环境变量，不要把 key 提交到 git（**当前 application.yml 里的 key 已经泄露过，建议立刻去 DeepSeek 控制台 revoke**）。

### 5.3 跑起来
```bash
./mvnw spring-boot:run
```
打开 http://localhost:8080/ 即可看到所有模块的入口导航。

### 5.4 各模块测试入口

| 模块         | 测试 URL（GET）                                                      |
| ------------ | -------------------------------------------------------------------- |
| 基础对话     | `/m1/chat?message=你好`                                              |
| 流式对话     | `/m1/stream?message=讲个笑话` （浏览器直接打开看 SSE）               |
| Prompt 工程  | `/m2/translate?text=Hello&target=日语`                               |
| 多轮记忆     | `/m3/chat?conversationId=u1&message=我叫小明`，再 `?message=我叫啥`  |
| 结构化输出   | `/m4/movie?title=星际穿越`                                           |
| 工具调用     | `/m5/ask?message=北京今天多少度，乘以 2 是多少？`                    |
| RAG          | 先 `POST /m6/load` 灌入文档，再 `/m6/ask?message=请假流程是什么？`   |
| ReAct Agent  | `/m7/run?task=查一下北京天气，再帮我算 32×7`                         |
| Workflow     | `/m8/chain?topic=红楼梦`、`/m8/route?question=...`                   |

---

## 6. 学习路径建议（每个模块预计 1–2 小时）

1. **读 README** → 知道这个模块要解决什么问题
2. **读 Controller** → 看入口和参数
3. **读 Service / Advisor / Tools** → 看 Spring AI 怎么把能力串起来
4. **改 prompt / 加一个 tool / 换个模型** → 真正建立直觉
5. **打开 DEBUG 日志** 看请求体里发给模型的完整 messages（这是理解 Agent 的关键）：
   ```yaml
   logging:
     level:
       org.springframework.ai: DEBUG
   ```

---

## 7. 从 Java 工程师视角理解几个关键概念

### 7.1 ChatClient ≈ RestTemplate
你以前写 `restTemplate.exchange(...)`，现在写 `chatClient.prompt().user(...).call().content()`。
区别在于「请求体」不是 JSON，而是 **结构化的对话历史**；「响应」可能是文本、JSON、工具调用指令。

### 7.2 Advisor ≈ HandlerInterceptor / Filter
所有「记忆、检索、日志、敏感词过滤、限流」这些**横切关注点**都做成 `Advisor`，链式装到 `ChatClient` 上。
这跟 Spring MVC 的拦截器链是**同一种心智模型**。

### 7.3 Tool / Function ≈ 暴露给「外部调用方」的接口
区别是调用方变成了 LLM。LLM 看到你的 `@Description` 决定调不调；它不直接执行 Java，而是返回一段 JSON 描述「我想调 weatherTool，参数是北京」，框架捕获后真正执行你的 Java 方法，再把结果回灌给 LLM。

### 7.4 RAG ≈ "智能版" ElasticSearch + 拼 prompt
传统全文检索：`查关键词 → 返回 top10 → 用户自己看`。
RAG：`把问题向量化 → 在向量库找相似切片 → 把切片塞进 prompt 让 LLM 总结回答`。
你已经写过的 `springboot-es` 项目就是 RAG 的「检索」一半，缺的是 embedding + 拼 prompt。

### 7.5 Agent ≈ 一个「会思考的 Service」
普通 Service：固定流程 `step1 → step2 → step3`。
Agent：`while (没完成) { 让 LLM 想下一步 → 执行 → 把结果给 LLM 再想 }`。
本质就是把流程控制权从代码交给了模型。

---

## 8. 后续可扩展方向（学完 8 个模块以后）

- **MCP（Model Context Protocol）**：Anthropic 提的开放协议，让任何工具/数据源都能以标准方式接入 Agent。Spring AI 已有 `spring-ai-mcp`。
- **多 Agent 协作**：用 workflow 模块做底座，扩展为 supervisor + 多个 specialist agent。
- **持久化 Memory**：把 `InMemoryChatMemory` 换成 Redis / 数据库实现。
- **向量库换 PgVector**：你日常的 PostgreSQL 直接当向量库。
- **Eval 评测集**：用 `spring-ai-evaluation` 或自建测试集，给每个模块写 eval test，**让 prompt 改动可以回归测试**。
- **可观测性**：接入 Spring AI 的 `Observation` + Micrometer + Tempo，把每次 LLM 调用做成可追踪 span。

---

## 9. 安全与成本须知

- **不要把 API Key 写进代码或 yml 提交到 git**。用环境变量或 Spring Cloud Config。
- **Prompt 注入**：用户输入会被拼进 prompt，可能让模型忽略 system 指令。生产里要做输入校验、敏感词过滤、最小工具权限。
- **成本**：每次调用都按 token 计费。开发期开 DEBUG 日志看请求大小，避免把大对象/全表数据塞进 prompt。RAG 要做切片大小控制和 top-k 限制。
- **超时与重试**：远程模型不稳定，建议在 Advisor 层加重试与熔断（用 Resilience4j）。

---

## 10. License & 致谢

学习项目，无 License 约束。参考与致谢：
- [Spring AI 官方文档](https://docs.spring.io/spring-ai/reference/)
- Anthropic《Building effective agents》
- LangChain / LangGraph 社区文章
