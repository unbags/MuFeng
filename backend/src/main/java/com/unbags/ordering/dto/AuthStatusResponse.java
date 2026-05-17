package com.unbags.ordering.dto;

public class AuthStatusResponse {

    private final boolean hasAdmin;

    public AuthStatusResponse(boolean hasAdmin) {
        this.hasAdmin = hasAdmin;
    }

    public boolean isHasAdmin() {
        return hasAdmin;
    }
}
