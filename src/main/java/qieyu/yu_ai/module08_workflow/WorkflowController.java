package qieyu.yu_ai.module08_workflow;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 模块 8 · Workflow 编排
 * <p>
 * 对应 Anthropic《Building effective agents》里的三种典型 workflow：
 *   1. Prompt Chaining（链）：上一步输出作为下一步输入。适合「分步骤稳定流程」。
 *   2. Parallelization（并行）：多个 LLM 调用并行跑，结果汇总。适合"多视角"评审。
 *   3. Routing（路由）：先用便宜模型分类，再分发到不同的下游 prompt / 模型。
 * <p>
 * 这一层不是新概念，本质就是把多次 ChatClient 调用编排起来。
 * 但思维上要从 "一次大调用" 转到 "多次小调用更可控、更便宜、更易调试"。
 */
@RestController
@RequestMapping("/m8")
public class WorkflowController {

    private final ChatClient chatClient;

    public WorkflowController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * Chain（串行链）：写文章三段式 —— 出大纲 → 写正文 → 起标题。
     * 比一次性"写一篇关于 X 的文章"更可控，每步可单独评审/重试。
     */
    @GetMapping("/chain")
    public Map<String, String> chain(@RequestParam String topic) {
        // step 1：出大纲
        String outline = chatClient.prompt()
                .user("请为关于「" + topic + "」的文章列出一个 3-5 点的大纲，仅输出大纲。")
                .call().content();

        // step 2：根据大纲写正文
        String article = chatClient.prompt()
                .user("根据以下大纲写一篇 300 字左右的文章：\n" + outline)
                .call().content();

        // step 3：起标题
        String title = chatClient.prompt()
                .user("为这篇文章起一个吸引人的标题（仅返回标题）：\n" + article)
                .call().content();

        return Map.of("title", title, "outline", outline, "article", article);
    }

    /**
     * Parallel（并行）：让 3 个"评委"独立评审同一段代码，最后汇总。
     * 用 CompletableFuture 并行触发，体感比串行快 3 倍。
     */
    @GetMapping("/parallel")
    public Map<String, Object> parallel(@RequestParam String code) {
        var perfFuture = CompletableFuture.supplyAsync(() -> chatClient.prompt()
                .system("你是性能优化专家，只关注性能问题。")
                .user(code).call().content());
        var secFuture = CompletableFuture.supplyAsync(() -> chatClient.prompt()
                .system("你是安全审计专家，只关注安全漏洞。")
                .user(code).call().content());
        var styleFuture = CompletableFuture.supplyAsync(() -> chatClient.prompt()
                .system("你是代码风格审查员，只关注命名、注释、格式。")
                .user(code).call().content());

        CompletableFuture.allOf(perfFuture, secFuture, styleFuture).join();

        String summary = chatClient.prompt()
                .user("汇总以下三份评审意见为一份执行清单：\n" +
                        "性能：" + perfFuture.join() + "\n" +
                        "安全：" + secFuture.join() + "\n" +
                        "风格：" + styleFuture.join())
                .call().content();

        return Map.of(
                "performance", perfFuture.join(),
                "security", secFuture.join(),
                "style", styleFuture.join(),
                "summary", summary
        );
    }

    /**
     * Routing（路由）：先分类问题类型，再分发到不同的 system prompt。
     * 思想：用一次便宜的"分类"调用，节省下游昂贵调用的 token；并提升专业度。
     */
    @GetMapping("/route")
    public Map<String, String> route(@RequestParam String question) {
        // step 1：分类
        String category = chatClient.prompt()
                .user("把以下问题归类，仅返回单词：technical / hr / sales / other。\n问题：" + question)
                .call().content().trim().toLowerCase();

        // step 2：按分类选不同 system prompt
        String systemPrompt = switch (category) {
            case "technical" -> "你是资深技术支持工程师，回答必须给出可复现的步骤。";
            case "hr" -> "你是 HR BP，回答必须基于公司制度，语气温和。";
            case "sales" -> "你是销售顾问，回答要突出价值与 ROI。";
            default -> "你是通用客服助手。";
        };

        String answer = chatClient.prompt()
                .system(systemPrompt)
                .user(question)
                .call().content();

        return Map.of("category", category, "answer", answer);
    }
}
