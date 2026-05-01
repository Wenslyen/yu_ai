package qieyu.yu_ai.module09_alibaba_ext;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 模块 9 · Spring AI Alibaba 独门绝技 · Prompt 热更新（Nacos）
 * <p>
 * 痛点：模块 2 把 prompt 写在 .st 文件里，改 prompt 要重新打包发布。
 * Spring AI Alibaba 提供 Nacos 集成，把 prompt 存进 Nacos 配置中心，
 * 修改 → 推送 → 应用 @RefreshScope 自动刷新，无需重启。
 * <p>
 * 启用步骤（默认未启用，避免没装 Nacos 的同学启动失败）：
 *   1) pom 加依赖：
 *      <dependency>
 *        <groupId>com.alibaba.cloud.ai</groupId>
 *        <artifactId>spring-ai-alibaba-starter-nacos-prompt</artifactId>
 *        <version>1.0.0-M5.1</version>
 *      </dependency>
 *   2) bootstrap.yml 加 Nacos 配置：
 *      spring:
 *        cloud:
 *          nacos:
 *            config:
 *              server-addr: localhost:8848
 *              namespace: public
 *      spring.ai.nacos.prompt.template.config.namespace: public
 *   3) Nacos 控制台新建配置 dataId = customerSupportPrompt.tpl，内容如本类常量。
 *   4) 把本类下方 PROMPT 字段改为：@Value("${customerSupportPrompt}") private String prompt;
 *   5) 类上加 @org.springframework.cloud.context.config.annotation.RefreshScope
 * <p>
 * 当前实现仅作 API 演示：从 application.yml 中读 prompt（也可在运行时通过 actuator/refresh 触发刷新）。
 */
@RestController
@RequestMapping("/m9")
public class NacosPromptController {

    /** 真实场景下这里是从 Nacos 读，热更新不重启。 */
    @Value("${app.prompts.customer-support:你是一名专业、耐心的客服助手。请用礼貌、简洁的中文回答客户问题。}")
    private String customerSupportPrompt;

    private final ChatClient chatClient;

    public NacosPromptController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @GetMapping("/prompt")
    public String ask(@RequestParam String question) {
        return chatClient.prompt()
                .system(customerSupportPrompt)
                .user(question)
                .call()
                .content();
    }
}
