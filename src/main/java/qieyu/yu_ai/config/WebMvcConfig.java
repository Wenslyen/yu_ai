package qieyu.yu_ai.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 强制 Spring MVC 用 UTF-8 写文本响应。
 * <p>
 * 背景：StringHttpMessageConverter 默认 ISO-8859-1，导致 SSE/纯文本流式接口
 * （Flux<String> + text/event-stream）写中文时被按 ISO-8859-1 编码，
 * 浏览器按 UTF-8 / GBK 解码都会乱码（典型现象：「绋嬪簭鍝」）。
 * 把它改成 UTF-8，全局接口写中文都不会乱。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        for (HttpMessageConverter<?> converter : converters) {
            if (converter instanceof StringHttpMessageConverter stringConverter) {
                stringConverter.setDefaultCharset(StandardCharsets.UTF_8);
            }
        }
    }
}
