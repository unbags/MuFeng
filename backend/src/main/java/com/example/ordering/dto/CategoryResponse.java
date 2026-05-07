package com.example.ordering.dto;

public class CategoryResponse {

    private String id;
    private String label;
    private Integer sortOrder;

    public CategoryResponse() {
    }

    public CategoryResponse(String id, String label, Integer sortOrder) {
        this.id = id;
        this.label = label;
        this.sortOrder = sortOrder;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
