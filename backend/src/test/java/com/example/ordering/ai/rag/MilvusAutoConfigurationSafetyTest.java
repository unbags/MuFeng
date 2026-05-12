package com.example.ordering.ai.rag;

import io.milvus.client.MilvusServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.milvus.autoconfigure.MilvusVectorStoreAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class MilvusAutoConfigurationSafetyTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withInitializer(new ConfigDataApplicationContextInitializer())
        .withConfiguration(AutoConfigurations.of(MilvusVectorStoreAutoConfiguration.class))
        .withUserConfiguration(TestEmbeddingModelConfiguration.class)
        .withPropertyValues(
            "spring.ai.vectorstore.milvus.client.host=192.0.2.1",
            "spring.ai.vectorstore.milvus.client.connect-timeout-ms=1"
        );

    @Test
    void explicitNoneVectorStoreTypeDoesNotCreateMilvusClient() {
        contextRunner
            .withPropertyValues("spring.ai.vectorstore.type=none")
            .run(context -> {
                assertThat(context).hasNotFailed();
                assertThat(context).doesNotHaveBean(MilvusServiceClient.class);
            });
    }

    @Configuration(proxyBeanMethods = false)
    static class TestEmbeddingModelConfiguration {

        @Bean
        EmbeddingModel embeddingModel() {
            return mock(EmbeddingModel.class);
        }
    }
}
