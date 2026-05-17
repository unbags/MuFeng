package com.unbags.ordering.ai.rag;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KnowledgeRefreshControllerRemovalTest {

    @Test
    void doesNotExposeAdminKnowledgeRefreshController() {
        assertThatThrownBy(() -> Class.forName("com.unbags.ordering.ai.rag.KnowledgeRefreshController"))
            .isInstanceOf(ClassNotFoundException.class);
    }
}
