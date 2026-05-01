package qieyu.yu_ai.module05_tools.tools;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.function.Function;

/**
 * 工具：四则运算计算器。
 * <p>
 * 为什么 LLM 也需要计算器？
 * 大模型的算术能力其实很弱（尤其是大数 / 多位小数），让它"调工具"算比"自己算"靠谱得多。
 * 这就是 Tools 的核心价值：把模型擅长的（推理、自然语言）和不擅长的（精确计算、数据查询）解耦。
 */
@Configuration
public class CalculatorTools {

    public record CalcRequest(double a, double b, String op) {}
    public record CalcResponse(double result) {}

    @Bean
    @Description("数学计算工具。op 取值：add 加 / sub 减 / mul 乘 / div 除。")
    public Function<CalcRequest, CalcResponse> calculatorTool() {
        return req -> {
            double r = switch (req.op()) {
                case "add" -> req.a() + req.b();
                case "sub" -> req.a() - req.b();
                case "mul" -> req.a() * req.b();
                case "div" -> req.b() == 0 ? Double.NaN : req.a() / req.b();
                default -> throw new IllegalArgumentException("unknown op: " + req.op());
            };
            return new CalcResponse(r);
        };
    }
}
