package qieyu.yu_ai.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 旧版示例 Controller，保留以兼容历史 URL <code>/chat/ai</code>。
 * 新增模块请放到 module01_chat ~ module09_alibaba_ext 下。
 */
@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatClient chatClient;

    public ChatController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @GetMapping("/ai")
    String generation(String message) {
        return this.chatClient.prompt()
                .user(message)
                .call()
                .content();
    }
}
