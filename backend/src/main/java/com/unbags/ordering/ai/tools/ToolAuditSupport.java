package com.unbags.ordering.ai.tools;

import com.unbags.ordering.ai.assistant.AiAssistantProperties;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class ToolAuditSupport {

    private final ToolAuditService auditService;
    private final AiAssistantProperties properties;

    public ToolAuditSupport(ToolAuditService auditService, AiAssistantProperties properties) {
        this.auditService = auditService;
        this.properties = properties;
    }

    public static ToolAuditSupport disabled() {
        return new ToolAuditSupport(null, null);
    }

    public <T> T record(String toolName, String parameterSummary, Supplier<T> action) {
        if (!auditEnabled()) {
            return action.get();
        }

        long start = System.currentTimeMillis();
        try {
            T result = action.get();
            auditService.record(toolName, parameterSummary, elapsedSince(start), true, null);
            return result;
        } catch (RuntimeException exception) {
            auditService.record(toolName, parameterSummary, elapsedSince(start), false, exception.getMessage());
            throw exception;
        }
    }

    private boolean auditEnabled() {
        return auditService != null
            && properties != null
            && properties.getTools() != null
            && properties.getTools().isAuditEnabled();
    }

    private long elapsedSince(long start) {
        return Math.max(0L, System.currentTimeMillis() - start);
    }
}
