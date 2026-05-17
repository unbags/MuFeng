package com.unbags.ordering.dto;

public class ApiErrorResponse {

    private final int code;
    private final String message;
    private final Object data;

    public ApiErrorResponse(int code, String message) {
        this.code = code;
        this.message = message;
        this.data = null;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Object getData() {
        return data;
    }
}
