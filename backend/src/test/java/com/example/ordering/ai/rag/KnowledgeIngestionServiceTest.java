package com.example.ordering.ai.rag;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class KnowledgeIngestionServiceTest {

    @Test
    @SuppressWarnings("unchecked")
    void convertsKnowledgeDocumentsBeforeWritingToVectorStore() {
        VectorStore vectorStore = mock(VectorStore.class);
        KnowledgeIngestionService service = new KnowledgeIngestionService(vectorStore);

        service.ingest(List.of(new KnowledgeDocument(
            "菜品：南瓜鸡肉能量碗",
            Map.of("doc_type", "dish", "source_id", "dish:1001")
        )));

        org.mockito.ArgumentCaptor<List<Document>> captor = org.mockito.ArgumentCaptor.forClass(List.class);
        verify(vectorStore).add(captor.capture());
        assertThat(captor.getValue()).hasSize(1);
        assertThat(captor.getValue().get(0).getText()).contains("南瓜鸡肉能量碗");
        assertThat(captor.getValue().get(0).getMetadata()).containsEntry("source_id", "dish:1001");
    }

    @Test
    void skipsVectorStoreCallWhenThereAreNoDocuments() {
        VectorStore vectorStore = mock(VectorStore.class);
        KnowledgeIngestionService service = new KnowledgeIngestionService(vectorStore);

        int written = service.ingest(List.of());

        assertThat(written).isZero();
    }
}
