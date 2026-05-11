package com.example.ordering.ai.prompt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PromptTemplateService {

    private final ResourceLoader resourceLoader;
    private final String location;
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    public PromptTemplateService(
        ResourceLoader resourceLoader,
        @Value("${app.ai.prompt.location:classpath:/prompts}") String location
    ) {
        this.resourceLoader = resourceLoader;
        this.location = trimTrailingSlash(location);
    }

    /**
     * 渲染指定提示词模板，并用传入变量替换模板占位符。
     */
    public String render(String templateName, Map<String, ?> variables) {
        String template = load(templateName);
        String rendered = template;
        for (Map.Entry<String, ?> entry : variables.entrySet()) {
            String value = entry.getValue() == null ? "" : String.valueOf(entry.getValue());
            rendered = rendered.replace("{" + entry.getKey() + "}", value);
        }
        return rendered;
    }

    /**
     * 从缓存读取模板，缓存未命中时加载模板文件。
     */
    private String load(String templateName) {
        return cache.computeIfAbsent(templateName, this::readTemplate);
    }

    /**
     * 从配置的位置读取提示词模板文件。
     */
    private String readTemplate(String templateName) {
        Resource resource = resourceLoader.getResource(location + "/" + templateName + ".st");
        if (!resource.exists()) {
            throw new IllegalArgumentException("提示词模板不存在：" + templateName);
        }
        try {
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("提示词模板读取失败：" + templateName, e);
        }
    }

    /**
     * 清理模板目录末尾的斜杠，并为空配置提供默认目录。
     */
    private String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "classpath:/prompts";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
