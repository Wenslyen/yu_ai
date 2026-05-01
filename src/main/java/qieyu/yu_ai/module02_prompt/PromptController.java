package qieyu.yu_ai.module02_prompt;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 模块 2 · Prompt 工程
 * <p>
 * 教学目标：
 * 1. 区分 System 与 User 两种角色 —— System 设人设/规则，User 给具体任务。
 * 2. 用 PromptTemplate 占位符 ({param}) 解耦「模板」和「数据」。
 * 3. 把模板放进 resources/prompts/*.st 文件，让 prompt 可以独立改、独立评审。
 * <p>
 * 类比记忆：System Prompt ≈ 接口契约 + 全局规则；占位符 ≈ MyBatis 的 #{}。
 */
@RestController
@RequestMapping("/m2")
public class PromptController {

    private final ChatClient chatClient;

    /** 从 classpath 加载外部 prompt 模板，prompt 改动不需要改 Java 代码。 */
    @Value("classpath:/prompts/translator.st")
    private Resource translatorPrompt;

    @Value("classpath:/prompts/code-reviewer.st")
    private Resource codeReviewerPrompt;

    public PromptController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * 翻译机器人：演示 system prompt + 模板占位符。
     * 例：/m2/translate?text=Hello world&target=日语
     */
    @GetMapping("/translate")
    public String translate(@RequestParam String text, @RequestParam String target) {
        return chatClient.prompt()
                .system(spec -> spec.text(translatorPrompt).param("target", target))
                .user(text)
                .call()
                .content();
    }

    /**
     * 代码评审机器人：演示「资深角色」型 system prompt。
     * <p>
     * 改用 POST + 纯文本请求体：代码里常见的 {} () ; 等字符在 GET URL 里需要 URL 编码，
     * Tomcat 严格 URI 校验直接 400。POST body 里随便写。
     * <p>
     * 调用示例：
     *   curl -X POST 'http://localhost:8080/m2/review' \
     *        -H 'Content-Type: text/plain' \
     *        --data-binary 'public int add(int a,int b){return a+b;}'
     */
    @PostMapping(value = "/review", consumes = MediaType.TEXT_PLAIN_VALUE)
    public String review(@RequestBody String code) {
        return chatClient.prompt()
                .system(codeReviewerPrompt)
                .user(code)
                .call()
                .content();
    }
}
