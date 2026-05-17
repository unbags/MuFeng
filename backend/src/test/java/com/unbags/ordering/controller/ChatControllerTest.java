package com.unbags.ordering.controller;

import com.unbags.ordering.ai.assistant.AiAssistantService;
import com.unbags.ordering.dto.ChatRequest;
import com.unbags.ordering.dto.ChatResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChatControllerTest {

    @Test
    void usesCartIdHeaderWhenRequestBodyDoesNotContainCartId() {
        AiAssistantService aiAssistantService = mock(AiAssistantService.class);
        when(aiAssistantService.reply(org.mockito.ArgumentMatchers.any(ChatRequest.class)))
            .thenReturn(new ChatResponse("好的", "AI", LocalDateTime.now()));
        ChatController controller = new ChatController(aiAssistantService);
        HttpSession session = mock(HttpSession.class);
        when(session.getId()).thenReturn("SESSION-1");

        ChatRequest request = new ChatRequest("再来一份芒果酸奶碗", null);

        controller.query("cart-stable-1", request, session);

        ArgumentCaptor<ChatRequest> captor = forClass(ChatRequest.class);
        verify(aiAssistantService).reply(captor.capture());
        assertThat(captor.getValue().getCartId()).isEqualTo("cart-stable-1");
    }
}
