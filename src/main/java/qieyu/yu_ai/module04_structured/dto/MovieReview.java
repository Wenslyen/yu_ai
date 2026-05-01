package qieyu.yu_ai.module04_structured.dto;

import java.util.List;

/**
 * 给 LLM 的"输出契约"。
 * 字段名/注释会被 BeanOutputConverter 转成 JSON Schema 一并塞进 prompt，
 * 模型收到后会按这个 schema 输出严格的 JSON。
 *
 * 用 record 因为：
 *   1) 不可变，符合"输出对象"语义。
 *   2) Spring AI 1.0 对 record 解析支持很好。
 */
public record MovieReview(
        String title,
        String director,
        Integer year,
        Double rating,        // 0-10
        List<String> genres,  // 例如 ["科幻", "剧情"]
        String summary,       // 一句话剧情
        List<String> highlights,  // 3-5 个看点
        String recommendation     // 一句话推荐语
) {}
