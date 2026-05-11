package com.example.ordering.dto;

public class ApiResponse<T> {

    private final int code;
    private final String message;
    private final T data;

    private ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 构造成功响应，默认返回中文成功提示。
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "成功", data);
    }

    /**
     * 构造带自定义提示的成功响应。
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data);
    }

    /**
     * 构造失败响应，携带业务错误码和中文错误提示。
     */
    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    /**
     * 返回接口响应码。
     */
    public int getCode() {
        return code;
    }

    /**
     * 返回接口提示信息。
     */
    public String getMessage() {
        return message;
    }

    /**
     * 返回接口业务数据。
     */
    public T getData() {
        return data;
    }
}
