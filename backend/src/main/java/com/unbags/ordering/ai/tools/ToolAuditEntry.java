package com.unbags.ordering.ai.tools;

import java.time.OffsetDateTime;

public class ToolAuditEntry {

    private String toolName;
    private String parameterSummary;
    private long durationMillis;
    private boolean success;
    private String errorMessage;
    private OffsetDateTime recordedAt;

    public ToolAuditEntry() {
    }

    public ToolAuditEntry(String toolName, String parameterSummary, long durationMillis,
                          boolean success, String errorMessage, OffsetDateTime recordedAt) {
        this.toolName = toolName;
        this.parameterSummary = parameterSummary;
        this.durationMillis = durationMillis;
        this.success = success;
        this.errorMessage = errorMessage;
        this.recordedAt = recordedAt;
    }

    public String toolName() {
        return toolName;
    }

    public String parameterSummary() {
        return parameterSummary;
    }

    public long durationMillis() {
        return durationMillis;
    }

    public boolean success() {
        return success;
    }

    public String errorMessage() {
        return errorMessage;
    }

    public OffsetDateTime recordedAt() {
        return recordedAt;
    }

    public String getToolName() {
        return toolName;
    }

    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    public String getParameterSummary() {
        return parameterSummary;
    }

    public void setParameterSummary(String parameterSummary) {
        this.parameterSummary = parameterSummary;
    }

    public long getDurationMillis() {
        return durationMillis;
    }

    public void setDurationMillis(long durationMillis) {
        this.durationMillis = durationMillis;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public OffsetDateTime getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(OffsetDateTime recordedAt) {
        this.recordedAt = recordedAt;
    }
}
