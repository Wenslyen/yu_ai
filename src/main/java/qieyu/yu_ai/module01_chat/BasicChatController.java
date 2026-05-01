package qieyu.yu_ai.module01_chat;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * 模块 1 · 基础对话
 * <p>
 * 教学目标：
 * 1. 体会 ChatClient 的「门面」用法 —— 一行链式调用就完成 LLM 请求。
 * 2. 看懂同步 vs. 流式的差异：
 *    - 同步 (call)：和普通 HTTP 调用一样，等模型出完整结果再返回。
 *    - 流式 (stream)：返回 Flux<String>，浏览器以 SSE 边收边显示，UX 更好。
 * <p>
 * 类比记忆：ChatClient ≈ RestTemplate / WebClient，只是请求体是「对话」。
 */
@RestController
@RequestMapping("/m1")
public class BasicChatController {

    private final ChatClient chatClient;

    public BasicChatController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /** 同步：一次性返回完整回答。 */
    @GetMapping("/chat")
    public String chat(@RequestParam String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }

    /**
     * 流式：浏览器直接打开能看到 token 一段段返回。
     * 用 SSE (text/event-stream) 协议，前端用 EventSource 即可消费。
     * <p>
     * 注意 produces 必须带 ;charset=UTF-8，否则浏览器按 GBK 解码会乱码。
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
    public Flux<String> stream(@RequestParam String message) {
        return chatClient.prompt()
                .user(message)
                .stream()
                .content();
    }
}
