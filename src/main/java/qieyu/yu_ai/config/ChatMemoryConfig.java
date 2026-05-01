package qieyu.yu_ai.config;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 全局 ChatMemory 配置。
 * <p>
 * - InMemoryChatMemory：基于 ConcurrentHashMap，按 conversationId 存对话历史。
 * - 学习场景够用；生产环境换成 Redis / DB 实现 ChatMemory 接口即可。
 */
@Configuration
public class ChatMemoryConfig {

    @Bean
    public ChatMemory chatMemory() {
        return new InMemoryChatMemory();
    }
}
