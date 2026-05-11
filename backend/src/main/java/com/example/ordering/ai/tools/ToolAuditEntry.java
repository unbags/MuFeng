package com.example.ordering.ai.tools;

import java.time.OffsetDateTime;

public record ToolAuditEntry(
    String toolName,
    String parameterSummary,
    long durationMillis,
    boolean success,
    String errorMessage,
    OffsetDateTime recordedAt
) {
}
