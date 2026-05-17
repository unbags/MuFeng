package com.unbags.ordering.ai.rag;

import com.unbags.ordering.ai.assistant.AiAssistantProperties;
import com.unbags.ordering.dto.MenuResponse;
import com.unbags.ordering.service.MenuService;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KnowledgeRefreshServiceTest {

    @Test
    void refreshesMenuAndBusinessKnowledgeWhenRagIsAvailable() throws Exception {
        KnowledgeDocumentBuilder builder = mock(KnowledgeDocumentBuilder.class);
        KnowledgeIngestionService ingestionService = mock(KnowledgeIngestionService.class);
        MenuService menuService = mock(MenuService.class);
        AiAssistantProperties properties = new AiAssistantProperties();
        properties.getRag().setEnabled(true);
        MenuResponse menu = new MenuResponse(Collections.emptyList(), Collections.emptyList());

        when(ingestionService.isAvailable()).thenReturn(true);
        when(menuService.getMenu()).thenReturn(menu);
        when(builder.fromMenu(menu)).thenReturn(List.of(new KnowledgeDocument("菜品：新品", Map.of("source_id", "dish:1"))));
        when(builder.fromMarkdownResource()).thenReturn(List.of(new KnowledgeDocument("营业时间", Map.of("source_id", "biz:hours"))));
        when(ingestionService.ingest(org.mockito.ArgumentMatchers.anyList())).thenReturn(2);

        KnowledgeRefreshService service = new KnowledgeRefreshService(builder, ingestionService, menuService, properties);

        KnowledgeRefreshService.RefreshResult result = service.refreshAllKnowledge();

        assertThat(result.refreshed()).isTrue();
        assertThat(result.documentCount()).isEqualTo(2);
        verify(ingestionService).clear();
        verify(ingestionService).ingest(org.mockito.ArgumentMatchers.argThat(documents -> documents.size() == 2));
    }

    @Test
    void skipsRefreshWhenVectorStoreIsUnavailable() throws Exception {
        KnowledgeDocumentBuilder builder = mock(KnowledgeDocumentBuilder.class);
        KnowledgeIngestionService ingestionService = mock(KnowledgeIngestionService.class);
        MenuService menuService = mock(MenuService.class);
        AiAssistantProperties properties = new AiAssistantProperties();
        properties.getRag().setEnabled(true);
        when(ingestionService.isAvailable()).thenReturn(false);

        KnowledgeRefreshService service = new KnowledgeRefreshService(builder, ingestionService, menuService, properties);

        KnowledgeRefreshService.RefreshResult result = service.refreshAllKnowledge();

        assertThat(result.refreshed()).isFalse();
        verify(ingestionService, never()).clear();
        verify(builder, never()).fromMarkdownResource();
    }
}
