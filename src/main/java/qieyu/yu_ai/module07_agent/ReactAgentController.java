package qieyu.yu_ai.module07_agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 模块 7 · ReAct Agent
 * <p>
 * ReAct = Reasoning + Acting：让模型在 [Thought → Action → Observation] 循环里
 * 自己决定下一步做什么。Spring AI 1.0 还没有官方 Agent 抽象，所以这里用
 * 「强 system prompt + tools + 多轮自动迭代」的方式手搓一个最小 Agent。
 * <p>
 * 关键点：
 *   1. system prompt 明确要求模型按 ReAct 范式工作（先想、再调工具、看结果、再想）。
 *   2. 把 module05 注册的所有工具都暴露给模型。
 *   3. Spring AI 自动多轮工具调用：模型只要还在 return tool_call，框架会一直循环执行。
 * <p>
 * 测试：
 *   /m7/run?task=查一下北京天气，再算这个温度乘以 7 等于多少
 *   /m7/run?task=今天几号？再告诉我深圳天气
 */
@RestController
@RequestMapping("/m7")
public class ReactAgentController {

    private static final String AGENT_SYSTEM = """
            你是一个 ReAct 风格的智能助手。处理用户任务时严格按以下范式思考：

            1. Thought: 分析任务，判断是否需要调用工具，需要哪个工具。
            2. Action: 调用合适的工具（已注册：weatherTool、calculatorTool、dateTimeTool）。
            3. Observation: 拿到工具返回值后，判断是否已经能回答用户。
            4. 循环以上，直到任务完成。
            5. Final Answer: 用自然语言给出最终答复。

            约束：
            - 一次只调用必要的工具，不要调用与任务无关的工具。
            - 涉及计算时必须调 calculatorTool，不要自己心算。
            - 涉及天气、时间时必须调对应工具。
            - 如果工具不足以解决问题，明确告诉用户。
            """;

    private final ChatClient chatClient;

    public ReactAgentController(ChatClient.Builder builder) {
        this.chatClient = builder
                .defaultSystem(AGENT_SYSTEM)
                .defaultFunctions("weatherTool", "calculatorTool", "dateTimeTool")
                .build();
    }

    @GetMapping("/run")
    public String run(@RequestParam String task) {
        return chatClient.prompt()
                .user(task)
                .call()
                .content();
    }
}
