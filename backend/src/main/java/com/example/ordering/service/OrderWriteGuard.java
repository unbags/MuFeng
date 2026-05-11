package com.example.ordering.service;

import com.example.ordering.config.HighConcurrencyProperties;
import org.springframework.stereotype.Component;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Component
public class OrderWriteGuard {

    private final Semaphore semaphore;

    public OrderWriteGuard(HighConcurrencyProperties properties) {
        this.semaphore = new Semaphore(properties.getMaxConcurrentOrderWrites(), true);
    }

    /**
     * 尝试获取下单写入许可，避免高并发下同时写入过多订单。
     */
    public boolean tryAcquire() {
        try {
            return semaphore.tryAcquire(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * 释放下单写入许可。
     */
    public void release() {
        semaphore.release();
    }
}
