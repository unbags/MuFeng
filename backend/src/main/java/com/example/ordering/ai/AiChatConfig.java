package com.example.ordering.ai;

import com.example.ordering.ai.assistant.AiAssistantProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConditionalOnProperty(name = "app.ai.enabled", havingValue = "true")
public class AiChatConfig {

    private static final Logger log = LoggerFactory.getLogger(AiChatConfig.class);

    /**
     * 创建聊天记忆组件，用于保留最近多轮会话上下文。
     */
    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
            .chatMemoryRepository(new InMemoryChatMemoryRepository())
            .maxMessages(10)
            .build();
    }

    /**
     * 创建 AI 聊天客户端，并按配置挂载记忆、知识库检索和工具调用能力。
     */
    @Bean
    @ConditionalOnBean(ChatModel.class)
    public ChatClient chatClient(ChatModel chatModel,
                                  ChatMemory chatMemory,
                                  AiAssistantProperties properties,
                                  ObjectProvider<VectorStore> vectorStoreProvider,
                                  ObjectProvider<ToolCallback> toolCallbackProvider) {

        ChatClient.Builder builder = ChatClient.builder(chatModel);

        builder.defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build());

        if (properties.getRag().isEnabled()) {
            VectorStore vectorStore = vectorStoreProvider.getIfAvailable();
            if (vectorStore != null) {
                builder.defaultAdvisors(QuestionAnswerAdvisor.builder(vectorStore).build());
            } else {
                log.warn("RAG is enabled (app.ai.rag.enabled=true) but no VectorStore bean is available. "
                    + "Check that spring.ai.model.embedding is set to a valid embedding model "
                    + "and spring.ai.vectorstore.type is set to milvus.");
            }
        }

        if (properties.getTools().isEnabled()) {
            List<ToolCallback> toolCallbacks = toolCallbackProvider.stream().toList();
            if (!toolCallbacks.isEmpty()) {
                builder.defaultToolCallbacks(toolCallbacks);
            }
        }

        return builder.build();
    }
}
