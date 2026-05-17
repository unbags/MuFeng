package com.unbags.ordering.dto;

import java.util.List;

public class PageResponse<T> {

    private final List<T> items;
    private final long total;
    private final long page;
    private final long size;

    public PageResponse(List<T> items, long total, long page, long size) {
        this.items = items;
        this.total = total;
        this.page = page;
        this.size = size;
    }

    public List<T> getItems() {
        return items;
    }

    public long getTotal() {
        return total;
    }

    public long getPage() {
        return page;
    }

    public long getSize() {
        return size;
    }
}
