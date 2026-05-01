package qieyu.yu_ai.module09_alibaba_ext;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.Media;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URL;
import java.util.List;

/**
 * 模块 9 · Spring AI Alibaba 独门绝技 · 多模态（看图）
 * <p>
 * 教学目标：演示 Qwen-VL 处理"图像 + 文本"输入。
 * <p>
 * 实现要点：
 *   1) 对公网图片，直接把 URL 交给 DashScope。Spring AI Alibaba M5.1 会把 byte[] 包装为
 *      data:image/...;base64,...，部分 Qwen-VL 接口会按 URL 校验并返回 url error。
 *   2) DashScopeChatOptions.withModel("qwen-vl-max-latest") 仅本次请求切多模态模型，
 *      不影响 module 1-8 默认走 qwen-plus。
 * <p>
 * 测试：
 *   /m9/vision?imageUrl=https://dashscope.oss-cn-beijing.aliyuncs.com/images/dog_and_girl.jpeg&question=图里有什么
 */
@RestController
@RequestMapping("/m9")
public class MultimodalController {

    private final DashScopeChatModel chatModel;

    public MultimodalController(DashScopeChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @GetMapping("/vision")
    public String vision(@RequestParam String imageUrl,
                         @RequestParam(defaultValue = "请描述这张图片的内容。") String question) throws Exception {
        // 1) 直接使用公网 URL。DashScope 多模态接口会自行拉取并校验图片 URL。
        Media image = new Media(guessMimeType(imageUrl), toPublicImageUrl(imageUrl));

        // 2) UserMessage 同时承载文本和图像
        UserMessage userMessage = new UserMessage(question, List.of(image));

        // 3) 临时切到 Qwen-VL 多模态模型；qwen-vl-max-latest 是当前最强且最新版本
        DashScopeChatOptions options = DashScopeChatOptions.builder()
                .withModel("qwen-vl-max-latest")
                .build();

        Prompt prompt = new Prompt(List.of(userMessage), options);
        return chatModel.call(prompt).getResult().getOutput().getText();
    }

    /** 只允许公网 http/https 图片 URL，避免 file:/classpath: 等本地地址被误传。 */
    private URL toPublicImageUrl(String imageUrl) throws Exception {
        URI uri = URI.create(imageUrl);
        String scheme = uri.getScheme();
        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            throw new IllegalArgumentException("imageUrl 只支持 http/https URL");
        }
        return uri.toURL();
    }

    /** 根据 URL 后缀粗略判断 MimeType；JPG 默认。 */
    private MimeType guessMimeType(String imageUrl) {
        String lower = imageUrl.toLowerCase();
        if (lower.endsWith(".png")) return MimeTypeUtils.IMAGE_PNG;
        if (lower.endsWith(".gif")) return MimeTypeUtils.IMAGE_GIF;
        if (lower.endsWith(".webp")) return MimeType.valueOf("image/webp");
        if (lower.endsWith(".bmp")) return MimeType.valueOf("image/bmp");
        return MimeTypeUtils.IMAGE_JPEG;
    }
}
