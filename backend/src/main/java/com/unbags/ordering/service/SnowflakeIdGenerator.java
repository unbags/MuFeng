package com.unbags.ordering.service;

import com.unbags.ordering.config.HighConcurrencyProperties;
import org.springframework.stereotype.Component;

@Component
public class SnowflakeIdGenerator {

    private static final long EPOCH = 1704067200000L;
    private static final long WORKER_ID_BITS = 5L;
    private static final long DATACENTER_ID_BITS = 5L;
    private static final long SEQUENCE_BITS = 12L;
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS);
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;

    private final long workerId;
    private final long datacenterId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    /**
     * 根据配置初始化雪花算法的机器编号和数据中心编号。
     */
    public SnowflakeIdGenerator(HighConcurrencyProperties properties) {
        if (properties.getWorkerId() > MAX_WORKER_ID || properties.getWorkerId() < 0) {
            throw new IllegalArgumentException("机器编号超出允许范围");
        }
        if (properties.getDatacenterId() > MAX_DATACENTER_ID || properties.getDatacenterId() < 0) {
            throw new IllegalArgumentException("数据中心编号超出允许范围");
        }
        this.workerId = properties.getWorkerId();
        this.datacenterId = properties.getDatacenterId();
    }

    /**
     * 生成全局唯一编号，用于订单、评价和业务记录主键。
     */
    public synchronized long nextId() {
        long currentTimestamp = currentTimestamp();
        if (currentTimestamp < lastTimestamp) {
            long offset = lastTimestamp - currentTimestamp;
            if (offset > 5000) {
                throw new IllegalStateException("系统时间异常，暂时无法生成订单编号");
            }
            try {
                Thread.sleep(offset + 1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("系统时间异常，暂时无法生成订单编号");
            }
            currentTimestamp = currentTimestamp();
        }

        if (currentTimestamp == lastTimestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0L) {
                currentTimestamp = waitUntilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = currentTimestamp;
        return ((currentTimestamp - EPOCH) << TIMESTAMP_SHIFT)
            | (datacenterId << DATACENTER_ID_SHIFT)
            | (workerId << WORKER_ID_SHIFT)
            | sequence;
    }

    /**
     * 在同一毫秒序列耗尽时等待下一毫秒。
     */
    private long waitUntilNextMillis(long lastTimestamp) {
        long timestamp = currentTimestamp();
        while (timestamp <= lastTimestamp) {
            try {
                Thread.sleep(0, 1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            timestamp = currentTimestamp();
        }
        return timestamp;
    }

    /**
     * 获取当前系统毫秒时间戳。
     */
    private long currentTimestamp() {
        return System.currentTimeMillis();
    }
}
