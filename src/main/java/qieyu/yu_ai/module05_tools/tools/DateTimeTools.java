package qieyu.yu_ai.module05_tools.tools;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

/**
 * 工具：当前时间。
 * <p>
 * 模型训练有截止时间，问"今天几号"它会答错或拒答；给一个 dateTimeTool 就能解决。
 */
@Configuration
public class DateTimeTools {

    public record EmptyRequest() {}
    public record DateTimeResponse(String now) {}

    @Bean
    @Description("获取当前服务器时间（ISO 格式）。当用户问今天几号、现在几点、当前时间时调用。")
    public Function<EmptyRequest, DateTimeResponse> dateTimeTool() {
        return req -> new DateTimeResponse(
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
    }
}
