package com.example.ordering.ai.tools;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ToolAuditServiceTest {

    @Test
    void recordsToolAuditEntryWithParameterSummary() {
        ToolAuditService service = new ToolAuditService();

        ToolAuditEntry entry = service.record("getOrderStatus", "orderNo=ORD1001", 12L, true, null);

        assertThat(entry.toolName()).isEqualTo("getOrderStatus");
        assertThat(entry.parameterSummary()).isEqualTo("orderNo=ORD1001");
        assertThat(entry.success()).isTrue();
        assertThat(entry.durationMillis()).isEqualTo(12L);
    }
}
