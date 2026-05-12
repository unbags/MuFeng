package com.example.ordering.ai.rag;

import com.example.ordering.ai.assistant.AiAssistantProperties;
import com.example.ordering.service.MenuService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class KnowledgeRefreshRunner implements ApplicationRunner {

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
        if (!ingestionService.isAvailable()) {
            log.warn("Knowledge initialization skipped: {}", KnowledgeIngestionService.VECTOR_STORE_UNAVAILABLE_MESSAGE);
            return;
        }

        try {
            ingestionService.clear();

            List<KnowledgeDocument> menuDocuments = documentBuilder.fromMenu(menuService.getMenu());
            List<KnowledgeDocument> bizDocuments = documentBuilder.fromMarkdownResource();

            List<KnowledgeDocument> allDocuments = new ArrayList<>(menuDocuments);
            allDocuments.addAll(bizDocuments);

            int count = ingestionService.ingest(allDocuments);
            log.info("Knowledge initialization complete: {} documents indexed ({} dishes + {} business rules)",
                count, menuDocuments.size(), bizDocuments.size());
        } catch (Exception e) {
            log.error("Knowledge initialization failed, RAG will not be available. "
                + "Retry from an operations task after the vector store is available.", e);
        }
    }
}
