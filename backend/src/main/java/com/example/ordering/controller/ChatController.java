package com.example.ordering.controller;

import com.example.ordering.dto.ApiResponse;
import com.example.ordering.dto.ChatRequest;
import com.example.ordering.dto.ChatResponse;
import com.example.ordering.service.ChatService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/query")
    public ApiResponse<ChatResponse> query(@Valid @RequestBody ChatRequest request) {
        return ApiResponse.success(chatService.reply(request));
    }
}
