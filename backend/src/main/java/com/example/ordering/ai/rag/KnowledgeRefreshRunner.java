package com.example.ordering.ai.rag;

import com.example.ordering.ai.assistant.AiAssistantProperties;
import com.example.ordering.service.MenuService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnBean(VectorStore.class)
public class

KnowledgeRefreshRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeRefreshRunner.class);

    private final KnowledgeDocumentBuilder documentBuilder;
    private final KnowledgeIngestionService ingestionService;
    private final MenuService menuService;
    private final AiAssistantProperties properties;

    public KnowledgeRefreshRunner(KnowledgeDocumentBuilder documentBuilder,
                                   KnowledgeIngestionService ingestionService,
                                   MenuService menuService,
                                   AiAssistantProperties properties) {
        this.documentBuilder = documentBuilder;
        this.ingestionService = ingestionService;
        this.menuService = menuService;
        this.properties = properties;
    }

    /**
     * 应用启动后根据配置刷新菜单知识库索引。
     */
    @Override
    public void run(ApplicationArguments args) {
        if (!properties.getRag().isEnabled()) {
            log.info("RAG is disabled, skipping knowledge initialization");
            return;
        }

        try {
            ingestionService.clear();
            List<KnowledgeDocument> documents = documentBuilder.fromMenu(menuService.getMenu());
            int count = ingestionService.ingest(documents);
            log.info("Knowledge initialization complete: {} documents indexed", count);
        } catch (Exception e) {
            log.error("Knowledge initialization failed, RAG will not be available. "
                + "Retry from an operations task after the vector store is available.", e);
        }
    }
}
