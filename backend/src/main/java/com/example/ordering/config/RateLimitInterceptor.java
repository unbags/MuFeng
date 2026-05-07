package com.example.ordering.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final int MAX_COUNTER_KEYS = 10000;

    private final Map<String, SlidingWindow> counters = new ConcurrentHashMap<>();
    private final boolean trustForwardedHeaders;

    public RateLimitInterceptor(@Value("${app.rate-limit.trust-forwarded-headers:false}") boolean trustForwardedHeaders) {
        this.trustForwardedHeaders = trustForwardedHeaders;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod method = (HandlerMethod) handler;
        RateLimit annotation = method.getMethodAnnotation(RateLimit.class);
        if (annotation == null) {
            return true;
        }
        cleanupExpiredWindows();

        String clientIp = getClientIp(request);
        String key = clientIp + ":" + request.getRequestURI();
        SlidingWindow window = counters.computeIfAbsent(key,
            k -> new SlidingWindow(annotation.windowSeconds()));

        if (!window.tryAcquire(annotation.maxRequests())) {
            response.setStatus(429);
            response.setContentType("application/json;charset=UTF-8");
            try {
                response.getWriter().write("{\"code\":429,\"message\":\"请求过于频繁，请稍后重试\",\"data\":null}");
            } catch (Exception ignored) {
            }
            return false;
        }

        return true;
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (trustForwardedHeaders && forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void cleanupExpiredWindows() {
        if (counters.size() < MAX_COUNTER_KEYS) {
            return;
        }
        counters.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    private static class SlidingWindow {
        private final long windowMillis;
        private volatile long windowStart;
        private volatile int count;

        SlidingWindow(int windowSeconds) {
            this.windowMillis = windowSeconds * 1000L;
            this.windowStart = System.currentTimeMillis();
        }

        synchronized boolean tryAcquire(int maxRequests) {
            long now = System.currentTimeMillis();
            if (now - windowStart > windowMillis) {
                windowStart = now;
                count = 0;
            }
            if (count >= maxRequests) {
                return false;
            }
            count++;
            return true;
        }

        boolean isExpired() {
            return System.currentTimeMillis() - windowStart > windowMillis;
        }
    }
}
