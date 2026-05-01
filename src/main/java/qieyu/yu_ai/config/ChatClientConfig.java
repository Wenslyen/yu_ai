package qieyu.yu_ai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 全局 ChatClient 配置。
 * <p>
 * 类比传统 Spring：ChatClient 之于 LLM ≈ RestTemplate / WebClient 之于 HTTP。
 * 这里给所有模块共享一个默认 ChatClient，并装上「日志 Advisor」方便观察请求。
 * 个别模块（如 RAG / Memory）会再 mutate 出自己的实例，加上额外 Advisor。
 */
@Configuration
public class ChatClientConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("""
                        你是一个乐于助人的中文 AI 助手。
                        - 回答简洁、准确，避免无意义客套话。
                        - 如果不确定，明确说明不确定。
                        """)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }
}
