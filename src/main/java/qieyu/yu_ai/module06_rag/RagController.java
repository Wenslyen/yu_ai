package qieyu.yu_ai.module06_rag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

/**
 * 模块 6 · RAG（Retrieval-Augmented Generation 检索增强生成）
 * <p>
 * RAG 解决的问题：
 *   - LLM 不知道你公司内部知识、最新数据、私有文档。
 *   - 把「相关资料」检索出来，拼进 prompt，再让 LLM 回答 → 模型成本不变但回答带上下文。
 * <p>
 * 数据流：
 *   原始文档 (PDF/MD/...) → Tika 解析 → 切片 (TokenTextSplitter) →
 *   每片向量化 (EmbeddingModel) → 存进 VectorStore →
 *   提问时把 question 也向量化 → 在 VectorStore 找最相似 top-k →
 *   QuestionAnswerAdvisor 把 top-k 拼进 prompt → LLM 回答
 * <p>
 * 测试流程：
 *   1) curl -X POST http://localhost:8080/m6/load
 *      （把 resources/docs/*.md 灌入向量库）
 *   2) curl 'http://localhost:8080/m6/ask?message=请假流程是什么？'
 */
@RestController
@RequestMapping("/m6")
public class RagController {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public RagController(ChatClient.Builder builder, VectorStore vectorStore) {
        this.vectorStore = vectorStore;
        // 本模块的 ChatClient 装上 QuestionAnswerAdvisor：
        // - similarityThreshold：相似度低于此值的切片被丢弃，避免引入噪声
        // - topK：最多取 4 条
        SearchRequest searchRequest = SearchRequest.builder()
                .topK(4)
                .similarityThreshold(0.5)
                .build();
        this.chatClient = builder
                .defaultAdvisors(new QuestionAnswerAdvisor(vectorStore, searchRequest))
                .build();
    }

    /** 灌库：扫描 classpath:/docs/ 下的所有 md 文件，切片 & 入向量库。 */
    @PostMapping("/load")
    public String load() throws IOException {
        var resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:/docs/*.md");
        var splitter = new TokenTextSplitter();
        int total = 0;
        for (Resource res : resources) {
            List<Document> raw = new TikaDocumentReader(res).read();
            List<Document> chunks = splitter.apply(raw);
            vectorStore.add(chunks);
            total += chunks.size();
        }
        return "已加载 " + resources.length + " 个文件，切分为 " + total + " 个片段";
    }

    /** RAG 提问：QuestionAnswerAdvisor 会自动检索并拼进 prompt。 */
    @GetMapping("/ask")
    public String ask(@RequestParam String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }
}
