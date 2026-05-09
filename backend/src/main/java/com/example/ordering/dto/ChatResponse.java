package com.example.ordering.dto;

import java.time.LocalDateTime;

public class ChatResponse {

    private String message;
    private String source;
    private LocalDateTime repliedAt;

    public ChatResponse() {
    }

    public ChatResponse(String message, String source, LocalDateTime repliedAt) {
        this.message = message;
        this.source = source;
        this.repliedAt = repliedAt;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public LocalDateTime getRepliedAt() {
        return repliedAt;
    }

    public void setRepliedAt(LocalDateTime repliedAt) {
        this.repliedAt = repliedAt;
    }
}
