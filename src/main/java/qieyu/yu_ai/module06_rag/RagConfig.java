package qieyu.yu_ai.module06_rag;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RAG 基础设施配置。
 * <p>
 * - EmbeddingModel：本地 transformers（通过 spring-ai-transformers-spring-boot-starter 自动注入）
 * - VectorStore：内存版 SimpleVectorStore，重启即清空。生产换 PgVector / Milvus / Redis Stack。
 */
@Configuration
public class RagConfig {

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return new SimpleVectorStore(embeddingModel);
    }
}
