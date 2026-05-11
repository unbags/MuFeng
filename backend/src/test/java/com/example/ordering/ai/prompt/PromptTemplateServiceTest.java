package com.example.ordering.ai.prompt;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PromptTemplateServiceTest {

    @Test
    void rendersTemplateWithNamedVariables() {
        PromptTemplateService service = new PromptTemplateService(
            new DefaultResourceLoader(),
            "classpath:/prompts"
        );
        Map<String, String> variables = new HashMap<>();
        variables.put("user_message", "两个人想吃清淡一点");
        variables.put("party_size", "2");
        variables.put("budget", "80 元以内");
        variables.put("dietary_restrictions", "不辣");
        variables.put("taste_preferences", "清淡");

        String rendered = service.render("dish-recommendation", variables);

        assertThat(rendered).contains("两个人想吃清淡一点", "80 元以内", "不辣");
        assertThat(rendered).doesNotContain("{user_message}");
    }

    @Test
    void rendersStaticTemplateWithoutVariables() {
        PromptTemplateService service = new PromptTemplateService(
            new DefaultResourceLoader(),
            "classpath:/prompts"
        );

        String rendered = service.render("tool-fallback", Collections.emptyMap());

        assertThat(rendered).contains("当前无法查询到相关数据");
    }

    @Test
    void doesNotShipAdminPromptTemplate() {
        assertThat(getClass().getClassLoader().getResource("prompts/admin-system.st")).isNull();
    }
}
