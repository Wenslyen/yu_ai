package qieyu.yu_ai.module05_tools;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 模块 5 · Function Calling / Tools
 * <p>
 * 测试用例（一个问题里包含多个工具调用）：
 *   /m5/ask?message=北京今天多少度，把这个温度乘以 2 是多少？
 *   /m5/ask?message=今天几号？
 * <p>
 * 观察日志能看到：模型先决定调 weatherTool，拿到 22 ℃，再调 calculatorTool 算 22*2=44。
 * 这就是 Agent 的雏形 —— 模型已经在"主动选择工具"了。
 */
@RestController
@RequestMapping("/m5")
public class ToolsController {

    private final ChatClient chatClient;

    public ToolsController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @GetMapping("/ask")
    public String ask(@RequestParam String message) {
        return chatClient.prompt()
                .user(message)
                // 工具按 Bean 名注册；想让模型只看到部分工具就只列部分名字
                .functions("weatherTool", "calculatorTool", "dateTimeTool")
                .call()
                .content();
    }
}
