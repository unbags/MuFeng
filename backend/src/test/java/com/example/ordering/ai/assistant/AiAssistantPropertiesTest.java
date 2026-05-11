package com.example.ordering.ai.assistant;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AiAssistantPropertiesTest {

    @Test
    void resolvesRuleBasedModeWhenAiIsDisabled() {
        AiAssistantProperties properties = new AiAssistantProperties();
        properties.setEnabled(false);
        properties.getRag().setEnabled(true);
        properties.getTools().setEnabled(true);

        assertThat(properties.resolveMode()).isEqualTo(AssistantMode.RULE_BASED);
    }

    @Test
    void resolvesRagOnlyModeWhenAiAndRagAreEnabledWithoutTools() {
        AiAssistantProperties properties = new AiAssistantProperties();
        properties.setEnabled(true);
        properties.getRag().setEnabled(true);
        properties.getTools().setEnabled(false);

        assertThat(properties.resolveMode()).isEqualTo(AssistantMode.RAG_ONLY);
    }

    @Test
    void resolvesFullAiModeWhenAiRagAndToolsAreEnabled() {
        AiAssistantProperties properties = new AiAssistantProperties();
        properties.setEnabled(true);
        properties.getRag().setEnabled(true);
        properties.getTools().setEnabled(true);

        assertThat(properties.resolveMode()).isEqualTo(AssistantMode.FULL_AI);
    }
}
