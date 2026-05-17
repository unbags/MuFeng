package com.unbags.ordering.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ChatResponse {

    private String message;
    private String source;
    private LocalDateTime repliedAt;
    private String intent;
    private List<ChatAction> actions = new ArrayList<>();
    private CartSnapshot cartSnapshot;
    private String clarification;

    public ChatResponse() {
    }

    public ChatResponse(String message, String source, LocalDateTime repliedAt) {
        this.message = message;
        this.source = source;
        this.repliedAt = repliedAt;
    }

    public ChatResponse(String message, String source, LocalDateTime repliedAt, String intent,
                        List<ChatAction> actions, CartSnapshot cartSnapshot, String clarification) {
        this.message = message;
        this.source = source;
        this.repliedAt = repliedAt;
        this.intent = intent;
        this.actions = actions == null ? new ArrayList<>() : actions;
        this.cartSnapshot = cartSnapshot;
        this.clarification = clarification;
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

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public List<ChatAction> getActions() {
        return actions;
    }

    public void setActions(List<ChatAction> actions) {
        this.actions = actions;
    }

    public CartSnapshot getCartSnapshot() {
        return cartSnapshot;
    }

    public void setCartSnapshot(CartSnapshot cartSnapshot) {
        this.cartSnapshot = cartSnapshot;
    }

    public String getClarification() {
        return clarification;
    }

    public void setClarification(String clarification) {
        this.clarification = clarification;
    }
}
