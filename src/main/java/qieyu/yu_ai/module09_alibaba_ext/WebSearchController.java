package qieyu.yu_ai.module09_alibaba_ext;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 模块 9 · Spring AI Alibaba 独门绝技 · 联网搜索
 * <p>
 * Qwen 系列模型支持「服务端联网搜索」：DashScope 在收到请求后，先用 Qwen 自身的搜索能力
 * 抓取相关网页（来源带白名单），把结果与原始 prompt 一起给模型生成。
 * <p>
 * 与自己写 RAG（模块 6）的区别：
 *   - RAG（自建）：你自己控制知识库内容，适合企业私有数据。
 *   - 联网搜索：让模型获取实时公网信息，适合时效性问题（今天股价、最新新闻）。
 * <p>
 * 这是 Spring AI 原生没有、Spring AI Alibaba 才有的能力。
 * <p>
 * 测试：
 *   /m9/search?q=2025年图灵奖得主是谁
 *   /m9/search?q=今天上证指数是多少
 */
@RestController
@RequestMapping("/m9")
public class WebSearchController {

    private final ChatClient chatClient;

    public WebSearchController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @GetMapping("/search")
    public String search(@RequestParam String q) {
        DashScopeChatOptions options = DashScopeChatOptions.builder()
                .withEnableSearch(true)   // 开启 DashScope 联网搜索
                .build();

        return chatClient.prompt()
                .options(options)
                .user(q)
                .call()
                .content();
    }
}
