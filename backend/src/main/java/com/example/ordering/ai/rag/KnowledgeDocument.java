package com.example.ordering.ai.rag;

import java.util.Map;

public record KnowledgeDocument(String content, Map<String, Object> metadata) {
}
