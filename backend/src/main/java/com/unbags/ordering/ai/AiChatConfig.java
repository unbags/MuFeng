package com.unbags.ordering.ai;

import com.unbags.ordering.ai.assistant.AiAssistantProperties;
import com.unbags.ordering.ai.tools.BusinessRuleTools;
import com.unbags.ordering.ai.tools.CartTools;
import com.unbags.ordering.ai.tools.MenuTools;
import com.unbags.ordering.ai.tools.OrderTools;
import com.unbags.ordering.ai.tools.RecommendTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
            .maxMessages(6)
            .build();
    }

    /**
     * 创建知识库检索增强顾问，仅在 RAG_FIRST 路由时按请求挂载，避免干扰工具调用。
     */
    @Bean
    @ConditionalOnBean(VectorStore.class)
    public QuestionAnswerAdvisor questionAnswerAdvisor(VectorStore vectorStore) {
        return QuestionAnswerAdvisor.builder(vectorStore).build();
    }

    @Bean
    @ConditionalOnBean(ChatModel.class)
    public ChatClient chatClient(ChatModel chatModel,
                                  ChatMemory chatMemory,
                                  AiAssistantProperties properties,
                                  ObjectProvider<VectorStore> vectorStoreProvider,
                                  MenuTools menuTools,
                                  CartTools cartTools,
                                  OrderTools orderTools,
                                  BusinessRuleTools businessRuleTools,
                                  RecommendTools recommendTools) {

        ChatClient.Builder builder = ChatClient.builder(chatModel);

        builder.defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build());

        // RAG advisor 不作为 defaultAdvisor，改为按请求路由在 AiAssistantService 中挂载

        if (properties.getRag().isEnabled()) {
            VectorStore vectorStore = vectorStoreProvider.getIfAvailable();
            if (vectorStore == null) {
                log.warn("RAG is enabled (app.ai.rag.enabled=true) but no VectorStore bean is available. "
                    + "Check that spring.ai.model.embedding is set to a valid embedding model "
                    + "and spring.ai.vectorstore.type is set to milvus.");
            }
        }

        if (properties.getTools().isEnabled()) {
            builder.defaultTools(menuTools, cartTools, orderTools, businessRuleTools, recommendTools);
            log.info("AI tools registered: getMenu, searchDishes, addToCart, updateCartItem, removeCartItem, getCart, getOrderStatus, getBusinessRules, recommendDishes");
        }

        return builder.build();
    }
}
