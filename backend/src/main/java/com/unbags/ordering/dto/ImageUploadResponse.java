package com.unbags.ordering.dto;

public class ImageUploadResponse {

    private final String url;

    public ImageUploadResponse(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
