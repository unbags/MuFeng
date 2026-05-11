package com.example.ordering.controller;

import com.example.ordering.ai.assistant.AiAssistantService;
import com.example.ordering.dto.ApiResponse;
import com.example.ordering.dto.ChatRequest;
import com.example.ordering.dto.ChatResponse;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

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
    public ApiResponse<ChatResponse> query(@Valid @RequestBody ChatRequest request) {
        return ApiResponse.success(aiAssistantService.reply(request));
    }

    /**
     * 接收顾客聊天问题，并以 SSE 流式返回智能助手回复。
     */
    @PostMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(@Valid @RequestBody ChatRequest request) {
        return aiAssistantService.stream(request);
    }
}
