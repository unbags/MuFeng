package com.unbags.ordering.ai.rag;

import java.util.Map;

public class KnowledgeDocument {

    private String content;
    private Map<String, Object> metadata;

    public KnowledgeDocument() {
    }

    public KnowledgeDocument(String content, Map<String, Object> metadata) {
        this.content = content;
        this.metadata = metadata;
    }

    public String content() {
        return content;
    }

    public Map<String, Object> metadata() {
        return metadata;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
