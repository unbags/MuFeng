package com.unbags.ordering.ai.tools;

import com.unbags.ordering.ai.assistant.AiAssistantProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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

    @Test
    void auditSupportRecordsSuccessfulToolCallWhenEnabled() {
        ToolAuditService auditService = mock(ToolAuditService.class);
        AiAssistantProperties properties = new AiAssistantProperties();
        properties.getTools().setAuditEnabled(true);
        ToolAuditSupport support = new ToolAuditSupport(auditService, properties);

        String result = support.record("getMenu", "all", () -> "ok");

        assertThat(result).isEqualTo("ok");
        verify(auditService).record(eq("getMenu"), eq("all"), anyLong(), eq(true), isNull());
    }

    @Test
    void auditSupportRecordsFailedToolCallAndRethrows() {
        ToolAuditService auditService = mock(ToolAuditService.class);
        AiAssistantProperties properties = new AiAssistantProperties();
        properties.getTools().setAuditEnabled(true);
        ToolAuditSupport support = new ToolAuditSupport(auditService, properties);

        assertThatThrownBy(() -> support.record("getOrderStatus", "orderNo=ORD1", () -> {
            throw new IllegalArgumentException("订单不存在");
        })).isInstanceOf(IllegalArgumentException.class);

        verify(auditService).record(eq("getOrderStatus"), eq("orderNo=ORD1"), anyLong(), eq(false), eq("订单不存在"));
    }
}
