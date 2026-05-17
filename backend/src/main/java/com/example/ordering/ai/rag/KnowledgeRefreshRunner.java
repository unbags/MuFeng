package com.example.ordering.ai.rag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class KnowledgeRefreshRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeRefreshRunner.class);

    private final KnowledgeRefreshService refreshService;

    public KnowledgeRefreshRunner(KnowledgeRefreshService refreshService) {
        this.refreshService = refreshService;
    }

    /**
     * 应用启动后根据配置刷新菜单知识库索引。
     */
    @Override
    public void run(ApplicationArguments args) {
        KnowledgeRefreshService.RefreshResult result = refreshService.refreshAllKnowledge();
        if (result.refreshed()) {
            log.info("Knowledge initialization complete: {} documents indexed ({} dishes + {} business rules)",
                result.documentCount(), result.menuDocumentCount(), result.businessDocumentCount());
        } else if (result.failed()) {
            log.warn("Knowledge initialization failed: {}", result.message());
        } else {
            log.info("Knowledge initialization skipped: {}", result.message());
        }
    }
}
