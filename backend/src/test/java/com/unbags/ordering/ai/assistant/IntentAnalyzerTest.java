package com.unbags.ordering.ai.assistant;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IntentAnalyzerTest {

    private final IntentAnalyzer analyzer = new IntentAnalyzer();

    @Test
    void explicitOrderExtractsDishNameAndChineseQuantity() {
        IntentAnalysisResult result = analyzer.analyze("我要两份招牌牛肉饭");

        assertThat(result.getIntent()).isEqualTo(AssistantIntent.EXPLICIT_ORDER);
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getRawName()).isEqualTo("招牌牛肉饭");
        assertThat(result.getItems().get(0).getQuantity()).isEqualTo(2);
        assertThat(result.isNeedsClarification()).isFalse();
    }

    @Test
    void transactionWithoutDishNeedsClarificationAndPaymentIsFlagged() {
        IntentAnalysisResult result = analyzer.analyze("帮我下单并付款");

        assertThat(result.getIntent()).isEqualTo(AssistantIntent.TRANSACTION);
        assertThat(result.getSafetyFlags().isRequestsCreateOrder()).isTrue();
        assertThat(result.getSafetyFlags().isRequestsPayment()).isTrue();
        assertThat(result.isNeedsClarification()).isTrue();
    }

    @Test
    void explicitDishWithPaymentKeepsItemAndFlagsTransaction() {
        IntentAnalysisResult result = analyzer.analyze("帮我下一份牛肉饭并付款");

        assertThat(result.getIntent()).isEqualTo(AssistantIntent.EXPLICIT_ORDER);
        assertThat(result.getSafetyFlags().isRequestsPayment()).isTrue();
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getRawName()).isEqualTo("牛肉饭");
        assertThat(result.getItems().get(0).getQuantity()).isEqualTo(1);
    }

    @Test
    void recommendationRequestDoesNotBecomeAmbiguousOrder() {
        IntentAnalysisResult result = analyzer.analyze("今日招牌菜推荐");

        assertThat(result.getIntent()).isEqualTo(AssistantIntent.RECOMMENDATION);
        assertThat(result.getItems()).isEmpty();
        assertThat(result.isNeedsClarification()).isFalse();
    }

    @Test
    void orderAfterRecommendationRemovesNaturalOrderingPrefix() {
        IntentAnalysisResult result = analyzer.analyze("我想点一份海盐拿铁");

        assertThat(result.getIntent()).isEqualTo(AssistantIntent.EXPLICIT_ORDER);
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getRawName()).isEqualTo("海盐拿铁");
        assertThat(result.getItems().get(0).getQuantity()).isEqualTo(1);
    }

    @Test
    void additionalOrderRemovesAgainPrefix() {
        IntentAnalysisResult result = analyzer.analyze("再来一份柑橘鲜虾沙拉");

        assertThat(result.getIntent()).isEqualTo(AssistantIntent.EXPLICIT_ORDER);
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getRawName()).isEqualTo("柑橘鲜虾沙拉");
        assertThat(result.getItems().get(0).getQuantity()).isEqualTo(1);
    }

    @Test
    void multiDishOrderExtractsEachDishAndQuantity() {
        IntentAnalysisResult result = analyzer.analyze("我想点一份香草牛肉意面、两份芒果酸奶碗");

        assertThat(result.getIntent()).isEqualTo(AssistantIntent.EXPLICIT_ORDER);
        assertThat(result.getItems()).hasSize(2);
        assertThat(result.getItems().get(0).getRawName()).isEqualTo("香草牛肉意面");
        assertThat(result.getItems().get(0).getQuantity()).isEqualTo(1);
        assertThat(result.getItems().get(1).getRawName()).isEqualTo("芒果酸奶碗");
        assertThat(result.getItems().get(1).getQuantity()).isEqualTo(2);
    }
}
