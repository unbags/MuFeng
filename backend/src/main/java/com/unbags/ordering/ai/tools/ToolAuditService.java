package com.unbags.ordering.ai.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class ToolAuditService {

    private static final Logger log = LoggerFactory.getLogger(ToolAuditService.class);

    public ToolAuditEntry record(
        String toolName,
        String parameterSummary,
        long durationMillis,
        boolean success,
        String errorMessage
    ) {
        ToolAuditEntry entry = new ToolAuditEntry(
            toolName,
            parameterSummary,
            durationMillis,
            success,
            errorMessage,
            OffsetDateTime.now()
        );
        log.info(
            "AI tool audit tool={} success={} durationMillis={} params={} error={}",
            entry.toolName(),
            entry.success(),
            entry.durationMillis(),
            entry.parameterSummary(),
            entry.errorMessage()
        );
        return entry;
    }
}
