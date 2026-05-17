package com.unbags.ordering.controller;

import com.unbags.ordering.ai.assistant.AiAssistantService;
import com.unbags.ordering.dto.ApiResponse;
import com.unbags.ordering.dto.ChatRequest;
import com.unbags.ordering.dto.ChatResponse;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final AiAssistantService aiAssistantService;

    public ChatController(AiAssistantService aiAssistantService) {
        this.aiAssistantService = aiAssistantService;
    }

    /**
     * 接收顾客聊天问题，并返回一次性智能助手回复。
     */
    @PostMapping("/query")
    public ApiResponse<ChatResponse> query(
        @RequestHeader(value = "X-Cart-Id", required = false) String cartId,
        @Valid @RequestBody ChatRequest request,
        HttpSession session
    ) {
        ensureCartId(request, cartId, session);
        return ApiResponse.success(aiAssistantService.reply(request));
    }

    /**
     * 接收顾客聊天问题，并以 SSE 流式返回智能助手回复。
     */
    @PostMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(
        @RequestHeader(value = "X-Cart-Id", required = false) String cartId,
        @Valid @RequestBody ChatRequest request,
        HttpSession session
    ) {
        ensureCartId(request, cartId, session);
        return aiAssistantService.stream(request)
            .concatWithValues("[DONE]");
    }

    private void ensureCartId(ChatRequest request, String headerCartId, HttpSession session) {
        if (request.getCartId() == null || request.getCartId().trim().isEmpty()) {
            if (headerCartId != null && !headerCartId.trim().isEmpty()) {
                request.setCartId(headerCartId.trim());
            } else {
                request.setCartId("session-" + session.getId());
            }
        }
    }
}
