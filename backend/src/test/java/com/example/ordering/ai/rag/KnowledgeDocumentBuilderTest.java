package com.example.ordering.ai.rag;

import com.example.ordering.dto.DishResponse;
import com.example.ordering.dto.MenuResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KnowledgeDocumentBuilderTest {

    @Test
    void buildsDishDocumentsWithTraceableMetadata() {
        DishResponse dish = new DishResponse();
        dish.setId(1001L);
        dish.setName("南瓜鸡肉能量碗");
        dish.setCategory("signature");
        dish.setPrice(new BigDecimal("32.00"));
        dish.setHighlight("暖胃清淡");
        dish.setDescription("适合想吃轻食的顾客");

        KnowledgeDocumentBuilder builder = new KnowledgeDocumentBuilder();

        List<KnowledgeDocument> documents = builder.fromMenu(new MenuResponse(Collections.emptyList(), List.of(dish)));

        assertThat(documents).hasSize(1);
        KnowledgeDocument document = documents.get(0);
        assertThat(document.content()).contains("南瓜鸡肉能量碗", "32.00", "暖胃清淡");
        assertThat(document.metadata())
            .containsEntry("doc_type", "dish")
            .containsEntry("source_id", "dish:1001")
            .containsEntry("status", "active");
    }
}
