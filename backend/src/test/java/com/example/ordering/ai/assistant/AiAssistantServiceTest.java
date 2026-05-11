package com.example.ordering.ai.assistant;

import com.example.ordering.ai.prompt.PromptTemplateService;
import com.example.ordering.dto.ChatRequest;
import com.example.ordering.dto.ChatResponse;
import com.example.ordering.service.ChatService;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AiAssistantServiceTest {

    @Test
    void routesOrderNumberQueriesToToolsFirst() {
        AiAssistantService service = service(fullAiProperties(), mock(PromptTemplateService.class));

        AssistantRoute route = service.route(new ChatRequest("帮我查订单", "ORD1001"));

        assertThat(route).isEqualTo(AssistantRoute.TOOL_FIRST);
    }

    @Test
    void routesRealtimeDataQuestionsToToolsFirst() {
        AiAssistantService service = service(fullAiProperties(), mock(PromptTemplateService.class));

        AssistantRoute route = service.route(new ChatRequest("南瓜鸡肉能量碗现在还有库存吗", null));

        assertThat(route).isEqualTo(AssistantRoute.TOOL_FIRST);
    }

    @Test
    void routesGeneralKnowledgeQuestionsToRagFirst() {
        AiAssistantService service = service(fullAiProperties(), mock(PromptTemplateService.class));

        AssistantRoute route = service.route(new ChatRequest("打包费规则是什么", null));

        assertThat(route).isEqualTo(AssistantRoute.RAG_FIRST);
    }

    @Test
    void rendersCustomerSystemPromptThroughTemplateService() {
        PromptTemplateService promptTemplateService = mock(PromptTemplateService.class);
        when(promptTemplateService.render("customer-system", Collections.emptyMap())).thenReturn("customer prompt");
        AiAssistantService service = service(new AiAssistantProperties(), promptTemplateService);

        assertThat(service.customerSystemPrompt()).isEqualTo("customer prompt");
    }

    @Test
    void fallsBackWhenAiCallReturnsBlankContent() {
        ChatService fallback = mock(ChatService.class);
        ChatRequest request = new ChatRequest("你好", null);
        when(fallback.reply(request)).thenReturn(new ChatResponse("今日推荐：南瓜鸡肉能量碗。", "MENU", LocalDateTime.now()));

        AiAssistantService service = serviceWithChatClient(fullAiProperties(), blankCallChatClient(), fallback);

        ChatResponse response = service.reply(request);

        assertThat(response.getMessage()).isEqualTo("今日推荐：南瓜鸡肉能量碗。");
        assertThat(response.getSource()).isEqualTo("MENU");
    }

    @Test
    void fallsBackWhenAiStreamCompletesWithoutChunks() {
        ChatService fallback = mock(ChatService.class);
        ChatRequest request = new ChatRequest("你好", null);
        when(fallback.reply(request)).thenReturn(new ChatResponse("今日推荐：南瓜鸡肉能量碗。", "MENU", LocalDateTime.now()));

        AiAssistantService service = serviceWithChatClient(fullAiProperties(), emptyStreamChatClient(), fallback);

        List<String> chunks = service.stream(request).collectList().block();

        assertThat(chunks).containsExactly("今日推荐：南瓜鸡肉能量碗。");
    }

    private AiAssistantProperties fullAiProperties() {
        AiAssistantProperties properties = new AiAssistantProperties();
        properties.setEnabled(true);
        properties.getRag().setEnabled(true);
        properties.getTools().setEnabled(true);
        return properties;
    }

    @SuppressWarnings("unchecked")
    private AiAssistantService service(AiAssistantProperties properties, PromptTemplateService promptTemplateService) {
        ObjectProvider<ChatClient> chatClientProvider = mock(ObjectProvider.class);
        when(chatClientProvider.getIfAvailable()).thenReturn(null);
        return new AiAssistantService(properties, promptTemplateService, mock(ChatService.class), chatClientProvider);
    }

    @SuppressWarnings("unchecked")
    private AiAssistantService serviceWithChatClient(AiAssistantProperties properties,
                                                    ChatClient chatClient,
                                                    ChatService fallback) {
        PromptTemplateService promptTemplateService = mock(PromptTemplateService.class);
        when(promptTemplateService.render("customer-system", Collections.emptyMap())).thenReturn("customer prompt");
        ObjectProvider<ChatClient> chatClientProvider = mock(ObjectProvider.class);
        when(chatClientProvider.getIfAvailable()).thenReturn(chatClient);
        return new AiAssistantService(properties, promptTemplateService, fallback, chatClientProvider);
    }

    private ChatClient blankCallChatClient() {
        ChatClient chatClient = mock(ChatClient.class);
        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec callResponseSpec = mock(ChatClient.CallResponseSpec.class);

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn(" ");

        return chatClient;
    }

    private ChatClient emptyStreamChatClient() {
        ChatClient chatClient = mock(ChatClient.class);
        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.StreamResponseSpec streamResponseSpec = mock(ChatClient.StreamResponseSpec.class);

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.stream()).thenReturn(streamResponseSpec);
        when(streamResponseSpec.content()).thenReturn(Flux.empty());

        return chatClient;
    }
}
