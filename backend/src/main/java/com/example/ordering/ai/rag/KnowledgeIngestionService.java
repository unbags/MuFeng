package com.example.ordering.ai.rag;

import io.milvus.client.MilvusServiceClient;
import io.milvus.param.collection.DropCollectionParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KnowledgeIngestionService {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeIngestionService.class);
    public static final String VECTOR_STORE_UNAVAILABLE_MESSAGE =
        "知识库向量服务未启用，请确认 application.yml 已配置 spring.ai.vectorstore.type=milvus，并已配置 DashScope API Key、启动 Milvus";

    private final ObjectProvider<VectorStore> vectorStoreProvider;

    public KnowledgeIngestionService(ObjectProvider<VectorStore> vectorStoreProvider) {
        this.vectorStoreProvider = vectorStoreProvider;
    }

    /**
     * 判断当前运行环境是否已经创建向量库 Bean。
     */
    public boolean isAvailable() {
        return vectorStoreProvider.getIfAvailable() != null;
    }

    private static final int EMBEDDING_BATCH_SIZE = 10;

    /**
     * 将知识文档分批写入向量库，每批最多 {@value #EMBEDDING_BATCH_SIZE} 条以符合 DashScope embedding API 限制。
     */
    public int ingest(List<KnowledgeDocument> documents) {
        if (documents == null || documents.isEmpty()) {
            return 0;
        }

        List<Document> springAiDocuments = documents.stream()
            .map(document -> new Document(document.content(), document.metadata()))
            .toList();

        int total = 0;
        for (int i = 0; i < springAiDocuments.size(); i += EMBEDDING_BATCH_SIZE) {
            int end = Math.min(i + EMBEDDING_BATCH_SIZE, springAiDocuments.size());
            List<Document> batch = springAiDocuments.subList(i, end);
            vectorStore().add(batch);
            total += batch.size();
            log.debug("Ingested batch {}-{} of {} documents", i + 1, end, springAiDocuments.size());
        }
        return total;
    }

    /**
     * 清空并重建 Milvus 知识库集合，供重新索引菜单知识使用。
     */
    public void clear() {
        try {
            VectorStore vectorStore = vectorStore();
            Object nativeClient = vectorStore.getNativeClient().orElse(null);
            if (nativeClient instanceof MilvusServiceClient milvusClient) {
                milvusClient.dropCollection(DropCollectionParam.newBuilder()
                    .withCollectionName("mufeng_kb")
                    .build());
                log.info("Dropped existing knowledge base collection");

                if (vectorStore instanceof InitializingBean initBean) {
                    initBean.afterPropertiesSet();
                    log.info("Reinitialized vector store schema");
                }
            } else {
                log.warn("Native client is not MilvusServiceClient, skipping clear. "
                    + "Manual collection cleanup may be required before reindexing.");
            }
        } catch (Exception e) {
            log.error("Failed to clear knowledge base. "
                + "Restart the application or manually drop the collection 'mufeng_kb'.", e);
        }
    }

    private VectorStore vectorStore() {
        VectorStore vectorStore = vectorStoreProvider.getIfAvailable();
        if (vectorStore == null) {
            throw new IllegalStateException(VECTOR_STORE_UNAVAILABLE_MESSAGE);
        }
        return vectorStore;
    }
}
