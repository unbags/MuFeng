package com.unbags.ordering.localization;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserFacingMessageLocalizationTest {

    private static final Path SOURCE_ROOT = Paths.get("src/main/java/com/unbags/ordering");

    @Test
    void apiSuccessAndPromptErrorsUseChineseMessages() throws IOException {
        List<Path> files = Arrays.asList(
            SOURCE_ROOT.resolve("dto/ApiResponse.java"),
            SOURCE_ROOT.resolve("ai/prompt/PromptTemplateService.java")
        );

        String source = readAll(files);

        assertThat(Files.exists(SOURCE_ROOT.resolve("ai/rag/KnowledgeRefreshController.java"))).isFalse();
        assertThat(source)
            .doesNotContain("\"ok\"")
            .doesNotContain("Prompt template not found")
            .doesNotContain("Failed to read prompt template");
    }

    private String readAll(List<Path> files) throws IOException {
        StringBuilder source = new StringBuilder();
        for (Path file : files) {
            source.append(new String(Files.readAllBytes(file), StandardCharsets.UTF_8)).append('\n');
        }
        return source.toString();
    }
}
