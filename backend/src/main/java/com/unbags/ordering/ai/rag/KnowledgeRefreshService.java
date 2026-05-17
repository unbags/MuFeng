package com.unbags.ordering.ai.rag;

import com.unbags.ordering.ai.assistant.AiAssistantProperties;
import com.unbags.ordering.dto.MenuResponse;
import com.unbags.ordering.service.MenuService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class KnowledgeRefreshService {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeRefreshService.class);

    private final KnowledgeDocumentBuilder documentBuilder;
    private final KnowledgeIngestionService ingestionService;
    private final MenuService menuService;
    private final AiAssistantProperties properties;

    public KnowledgeRefreshService(KnowledgeDocumentBuilder documentBuilder,
                                   KnowledgeIngestionService ingestionService,
                                   MenuService menuService,
                                   AiAssistantProperties properties) {
        this.documentBuilder = documentBuilder;
        this.ingestionService = ingestionService;
        this.menuService = menuService;
        this.properties = properties;
    }

    public RefreshResult refreshAllKnowledge() {
        if (!properties.getRag().isEnabled()) {
            return RefreshResult.skipped("RAG is disabled");
        }
        if (!ingestionService.isAvailable()) {
            return RefreshResult.skipped(KnowledgeIngestionService.VECTOR_STORE_UNAVAILABLE_MESSAGE);
        }

        try {
            ingestionService.clear();

            MenuResponse menu = menuService.getMenu();
            List<KnowledgeDocument> menuDocuments = documentBuilder.fromMenu(menu);
            List<KnowledgeDocument> businessDocuments = documentBuilder.fromMarkdownResource();

            List<KnowledgeDocument> allDocuments = new ArrayList<>(menuDocuments);
            allDocuments.addAll(businessDocuments);

            int count = ingestionService.ingest(allDocuments);
            return RefreshResult.refreshed(count, menuDocuments.size(), businessDocuments.size());
        } catch (Exception exception) {
            log.error("Knowledge refresh failed", exception);
            return RefreshResult.failed(exception.getMessage());
        }
    }

    public static class RefreshResult {
        private boolean refreshed;
        private boolean failed;
        private String message;
        private int documentCount;
        private int menuDocumentCount;
        private int businessDocumentCount;

        public RefreshResult() {
        }

        public RefreshResult(boolean refreshed, boolean failed, String message,
                             int documentCount, int menuDocumentCount, int businessDocumentCount) {
            this.refreshed = refreshed;
            this.failed = failed;
            this.message = message;
            this.documentCount = documentCount;
            this.menuDocumentCount = menuDocumentCount;
            this.businessDocumentCount = businessDocumentCount;
        }

        private static RefreshResult refreshed(int documentCount, int menuDocumentCount, int businessDocumentCount) {
            return new RefreshResult(true, false, "知识库刷新完成", documentCount, menuDocumentCount, businessDocumentCount);
        }

        private static RefreshResult skipped(String reason) {
            return new RefreshResult(false, false, reason, 0, 0, 0);
        }

        private static RefreshResult failed(String reason) {
            return new RefreshResult(false, true, reason, 0, 0, 0);
        }

        public boolean refreshed() {
            return refreshed;
        }

        public boolean failed() {
            return failed;
        }

        public String message() {
            return message;
        }

        public int documentCount() {
            return documentCount;
        }

        public int menuDocumentCount() {
            return menuDocumentCount;
        }

        public int businessDocumentCount() {
            return businessDocumentCount;
        }

        public boolean isRefreshed() {
            return refreshed;
        }

        public void setRefreshed(boolean refreshed) {
            this.refreshed = refreshed;
        }

        public boolean isFailed() {
            return failed;
        }

        public void setFailed(boolean failed) {
            this.failed = failed;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public int getDocumentCount() {
            return documentCount;
        }

        public void setDocumentCount(int documentCount) {
            this.documentCount = documentCount;
        }

        public int getMenuDocumentCount() {
            return menuDocumentCount;
        }

        public void setMenuDocumentCount(int menuDocumentCount) {
            this.menuDocumentCount = menuDocumentCount;
        }

        public int getBusinessDocumentCount() {
            return businessDocumentCount;
        }

        public void setBusinessDocumentCount(int businessDocumentCount) {
            this.businessDocumentCount = businessDocumentCount;
        }
    }
}
