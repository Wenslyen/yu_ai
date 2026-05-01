package qieyu.yu_ai.module05_tools.tools;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.function.Function;

/**
 * 工具：查询天气（Mock 实现）。
 * <p>
 * Spring AI Function Calling 的核心：
 *   1) Function<Req, Resp> 注册成 Bean。
 *   2) Bean 名 = 工具名（暴露给模型）。
 *   3) @Description 是给 LLM 看的「方法用途说明」，决定模型会不会调它。
 * <p>
 * 调用流程（框架替你处理）：
 *   user msg → LLM 决定调 weatherTool 并产出 JSON args → 框架反序列化为 Request →
 *   执行 Function → 拿到 Response → 序列化回 LLM → LLM 用结果生成最终回答。
 */
@Configuration
public class WeatherTools {

    public record WeatherRequest(String city) {}
    public record WeatherResponse(String city, double temperature, String description) {}

    @Bean
    @Description("根据城市名查询当前天气，返回温度和天气描述。当用户问天气、温度时调用此工具。")
    public Function<WeatherRequest, WeatherResponse> weatherTool() {
        return req -> {
            // Mock 数据；真实场景换成调和风/OpenWeather API
            double temp = switch (req.city()) {
                case "北京" -> 22.0;
                case "上海" -> 25.0;
                case "广州" -> 30.0;
                case "深圳" -> 29.5;
                case "杭州" -> 24.0;
                default -> 20.0;
            };
            return new WeatherResponse(req.city(), temp, "晴，微风");
        };
    }
}
