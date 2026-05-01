package qieyu.yu_ai.module04_structured;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import qieyu.yu_ai.module04_structured.dto.MovieReview;

import java.util.List;

/**
 * 模块 4 · 结构化输出
 * <p>
 * 业务现实：你不可能一直把 LLM 当「聊天框」，更多场景是
 * 「LLM 出一段结构化数据 → Java 代码消费 → 落库 / 转发」。
 * <p>
 * Spring AI 的做法：调用 .entity(SomeClass.class) 时，
 *   1) 自动把目标类型的 JSON Schema 塞进 prompt 末尾，告诉模型必须按这个结构出 JSON。
 *   2) 收到响应后用 Jackson 反序列化为对象，校验失败会抛异常。
 * <p>
 * 这是把 Agent 能力嵌进现有业务系统的最关键一步。
 */
@RestController
@RequestMapping("/m4")
public class StructuredOutputController {

    private final ChatClient chatClient;

    public StructuredOutputController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /** 单对象：电影 → MovieReview。 */
    @GetMapping("/movie")
    public MovieReview movie(@RequestParam String title) {
        return chatClient.prompt()
                .user(u -> u.text("给我一份电影《{title}》的介绍").param("title", title))
                .call()
                .entity(MovieReview.class);
    }

    /** 集合：列出 N 部某类型电影 → List<MovieReview>。 */
    @GetMapping("/movies")
    public List<MovieReview> movies(@RequestParam String genre,
                                    @RequestParam(defaultValue = "3") int count) {
        return chatClient.prompt()
                .user(u -> u.text("列出 {count} 部经典 {genre} 电影")
                        .param("count", count)
                        .param("genre", genre))
                .call()
                .entity(new org.springframework.core.ParameterizedTypeReference<>() {});
    }
}
