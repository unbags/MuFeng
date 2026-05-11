package com.example.ordering.ai.rag;

import io.milvus.client.MilvusServiceClient;
import io.milvus.param.collection.DropCollectionParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@ConditionalOnBean(VectorStore.class)
public class KnowledgeIngestionService {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeIngestionService.class);

    private final VectorStore vectorStore;

    public KnowledgeIngestionService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    /**
     * 将知识文档写入向量库，并返回实际写入的文档数量。
     */
    public int ingest(List<KnowledgeDocument> documents) {
        if (documents == null || documents.isEmpty()) {
            return 0;
        }

        List<Document> springAiDocuments = documents.stream()
            .map(document -> new Document(document.content(), document.metadata()))
            .toList();

        vectorStore.add(springAiDocuments);
        return springAiDocuments.size();
    }

    /**
     * 清空并重建 Milvus 知识库集合，供重新索引菜单知识使用。
     */
    public void clear() {
        try {
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
}
