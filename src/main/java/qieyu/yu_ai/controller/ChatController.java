package qieyu.yu_ai.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat")

public class ChatController {
    private final String PROMPT = "你好，我是AI助手，你有什么问题想问我吗？";

    /**
     *  模型类型
     *
     */
    @Value("${spring.ai.openai.chat.options.model}")
    private String model;
    private final ChatClient chatClient;
    @Autowired
    public ChatController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @GetMapping("/ai")
    String generation(String message) {
        //用户输入的信息提交给大模型，使用的是ChatClient与大模型交互。
        return this.chatClient.prompt()
                .user(message)
                .call()
                .content();
    }


}
