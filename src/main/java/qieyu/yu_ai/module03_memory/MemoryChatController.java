package qieyu.yu_ai.module03_memory;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

/**
 * 模块 3 · 多轮会话记忆
 * <p>
 * 核心问题：LLM 的 API 是无状态的 —— 每次调用都得自己把历史消息一并发过去。
 * Spring AI 用 ChatMemory + MessageChatMemoryAdvisor 把这件事自动化：
 *   1. 收到请求时：advisor 从 memory 取出该 conversationId 的历史，拼到 messages 里。
 *   2. 收到响应后：advisor 把这一轮 user/assistant 消息存回 memory。
 * <p>
 * 类比：servlet 是无状态的，靠 HttpSession 模拟会话；这里的 ChatMemory 就是「LLM 的 Session」。
 */
@RestController
@RequestMapping("/m3")
public class MemoryChatController {

    private final ChatClient chatClient;

    public MemoryChatController(ChatClient.Builder builder, ChatMemory chatMemory) {
        // 给当前模块单独的 ChatClient，装上 MemoryAdvisor
        this.chatClient = builder
                .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory))
                .build();
    }

    /**
     * 多轮对话。同一 conversationId 共享上下文。
     * <p>
     * 测试方式：
     *   1) /m3/chat?conversationId=u1&message=我叫小明
     *   2) /m3/chat?conversationId=u1&message=我叫什么名字
     *      → 应该答出「小明」
     *   3) 换个 conversationId=u2 不会受 u1 影响
     */
    @GetMapping("/chat")
    public String chat(@RequestParam String conversationId,
                       @RequestParam String message) {
        return chatClient.prompt()
                .user(message)
                .advisors(spec -> spec
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, conversationId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 20))  // 最多带 20 条历史
                .call()
                .content();
    }
}
