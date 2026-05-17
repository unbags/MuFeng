package com.unbags.ordering.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.high-concurrency")
public class HighConcurrencyProperties {

    private long workerId = 1L;
    private long datacenterId = 1L;
    private long menuCacheTtlSeconds = 30L;
    private int maxConcurrentOrderWrites = 60;

    public long getWorkerId() {
        return workerId;
    }

    public void setWorkerId(long workerId) {
        this.workerId = workerId;
    }

    public long getDatacenterId() {
        return datacenterId;
    }

    public void setDatacenterId(long datacenterId) {
        this.datacenterId = datacenterId;
    }

    public long getMenuCacheTtlSeconds() {
        return menuCacheTtlSeconds;
    }

    public void setMenuCacheTtlSeconds(long menuCacheTtlSeconds) {
        this.menuCacheTtlSeconds = menuCacheTtlSeconds;
    }

    public int getMaxConcurrentOrderWrites() {
        return maxConcurrentOrderWrites;
    }

    public void setMaxConcurrentOrderWrites(int maxConcurrentOrderWrites) {
        this.maxConcurrentOrderWrites = maxConcurrentOrderWrites;
    }
}
