package com.example.ordering.controller;

import com.example.ordering.ai.rag.KnowledgeDocument;
import com.example.ordering.ai.rag.KnowledgeIngestionService;
import com.example.ordering.dto.ApiResponse;
import com.example.ordering.dto.KnowledgeUploadResult;
import com.example.ordering.service.DocumentParsingService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KnowledgeControllerTest {

    @Test
    void reportsClearFailureWhenVectorStoreIsNotAvailable() {
        KnowledgeIngestionService ingestionService = mock(KnowledgeIngestionService.class);
        when(ingestionService.isAvailable()).thenReturn(false);
        KnowledgeController controller = new KnowledgeController(mock(DocumentParsingService.class), ingestionService);

        ApiResponse<List<KnowledgeUploadResult>> response = controller.upload(List.of(
            new MockMultipartFile("files", "faq.txt", "text/plain", "配送规则".getBytes())
        ));

        assertThat(response.getMessage()).isEqualTo("上传处理完成");
        assertThat(response.getData()).hasSize(1);
        KnowledgeUploadResult result = response.getData().get(0);
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).contains("知识库向量服务未启用");
    }

    @Test
    @SuppressWarnings("unchecked")
    void parsesAndIngestsFilesWhenVectorStoreIsAvailable() {
        DocumentParsingService parsingService = mock(DocumentParsingService.class);
        KnowledgeIngestionService ingestionService = mock(KnowledgeIngestionService.class);
        MockMultipartFile file = new MockMultipartFile("files", "faq.txt", "text/plain", "配送规则".getBytes());
        List<KnowledgeDocument> documents = List.of(new KnowledgeDocument("配送规则", Map.of("source_id", "faq.txt:0")));

        when(ingestionService.isAvailable()).thenReturn(true);
        when(parsingService.parse(file)).thenReturn(documents);
        when(ingestionService.ingest(documents)).thenReturn(1);

        KnowledgeController controller = new KnowledgeController(parsingService, ingestionService);

        ApiResponse<List<KnowledgeUploadResult>> response = controller.upload(List.of(file));

        verify(parsingService).validate(file);
        verify(ingestionService).ingest(documents);
        assertThat(response.getData()).hasSize(1);
        assertThat(response.getData().get(0).isSuccess()).isTrue();
        assertThat(response.getData().get(0).getChunkCount()).isEqualTo(1);
    }
}
