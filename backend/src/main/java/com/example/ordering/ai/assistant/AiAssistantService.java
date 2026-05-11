package com.example.ordering.ai.assistant;

import com.example.ordering.ai.prompt.PromptTemplateService;
import com.example.ordering.dto.ChatRequest;
import com.example.ordering.dto.ChatResponse;
import com.example.ordering.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.Collections;

@Service
public class AiAssistantService {

    private static final Logger log = LoggerFactory.getLogger(AiAssistantService.class);

    private final AiAssistantProperties properties;
    private final PromptTemplateService promptTemplateService;
    private final ChatService fallback;
    private final ChatClient chatClient;

    public AiAssistantService(AiAssistantProperties properties,
                               PromptTemplateService promptTemplateService,
                               ChatService fallback,
                               ObjectProvider<ChatClient> chatClientProvider) {
        this.properties = properties;
        this.promptTemplateService = promptTemplateService;
        this.fallback = fallback;
        this.chatClient = chatClientProvider.getIfAvailable();
    }

    /**
     * 解析当前智能助手运行模式。
     */
    public AssistantMode mode() {
        return properties.resolveMode();
    }

    /**
     * 根据用户请求判断优先走规则、工具调用还是知识库检索。
     */
    public AssistantRoute route(ChatRequest request) {
        if (mode() == AssistantMode.RULE_BASED) {
            return AssistantRoute.RULE_BASED;
        }

        String orderNo = normalize(request == null ? null : request.getOrderNo());
        String message = normalize(request == null ? null : request.getMessage());
        if (orderNo != null || containsAny(message, "订单", "order", "库存", "价格", "多少钱", "还有吗", "售罄")) {
            return AssistantRoute.TOOL_FIRST;
        }
        return AssistantRoute.RAG_FIRST;
    }

    /**
     * 渲染顾客助手的系统提示词。
     */
    public String customerSystemPrompt() {
        return promptTemplateService.render("customer-system", Collections.emptyMap());
    }

    /**
     * 生成一次性聊天回复，AI 不可用时自动回退到规则回复。
     */
    public ChatResponse reply(ChatRequest request) {
        if (chatClient == null || mode() == AssistantMode.RULE_BASED) {
            return fallback.reply(request);
        }

        String userMessage = buildUserMessage(request);
        String systemPrompt = customerSystemPrompt();

        try {
            String answer = chatClient.prompt()
                .user(userMessage)
                .system(systemPrompt)
                .call()
                .content();
            String normalizedAnswer = trimToNull(answer);
            if (normalizedAnswer == null) {
                log.warn("AI chat returned blank content, falling back to rule-based reply");
                return fallback.reply(request);
            }
            return new ChatResponse(normalizedAnswer, sourceTag(), LocalDateTime.now());
        } catch (Exception e) {
            log.error("AI chat failed, falling back to rule-based reply", e);
            return fallback.reply(request);
        }
    }

    /**
     * 生成流式聊天回复，流式调用失败时自动回退到规则回复。
     */
    public Flux<String> stream(ChatRequest request) {
        if (chatClient == null || mode() == AssistantMode.RULE_BASED) {
            ChatResponse reply = fallback.reply(request);
            return Flux.just(reply.getMessage());
        }

        String userMessage = buildUserMessage(request);
        String systemPrompt = customerSystemPrompt();

        try {
            return chatClient.prompt()
                .user(userMessage)
                .system(systemPrompt)
                .stream()
                .content()
                .filter(chunk -> trimToNull(chunk) != null)
                .switchIfEmpty(Flux.defer(() -> {
                    log.warn("AI stream returned no content, falling back to rule-based reply");
                    ChatResponse reply = fallback.reply(request);
                    return Flux.just(reply.getMessage());
                }))
                .onErrorResume(e -> {
                    log.error("AI stream failed, falling back to rule-based reply", e);
                    ChatResponse reply = fallback.reply(request);
                    return Flux.just(reply.getMessage());
                });
        } catch (Exception e) {
            log.error("AI stream failed, falling back to rule-based reply", e);
            ChatResponse reply = fallback.reply(request);
            return Flux.just(reply.getMessage());
        }
    }

    /**
     * 将前端请求整理为适合发送给模型的用户消息。
     */
    private String buildUserMessage(ChatRequest request) {
        StringBuilder sb = new StringBuilder();
        String orderNo = trimToNull(request.getOrderNo());
        String message = trimToNull(request.getMessage());

        if (orderNo != null) {
            sb.append("用户提供了订单号: ").append(orderNo).append("。");
        }
        if (message != null) {
            sb.append(" 用户问题: ").append(message);
        }
        if (sb.length() == 0) {
            sb.append("你好");
        }
        return sb.toString().trim();
    }

    /**
     * 根据当前模式生成回复来源标记。
     */
    private String sourceTag() {
        AssistantMode currentMode = mode();
        if (currentMode == AssistantMode.FULL_AI) {
            return "AI";
        }
        if (currentMode == AssistantMode.RAG_ONLY) {
            return "AI-RAG";
        }
        return "RULE";
    }

    /**
     * 判断文本是否包含任一关键词。
     */
    private boolean containsAny(String value, String... keywords) {
        if (value == null) {
            return false;
        }
        for (String keyword : keywords) {
            if (value.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 统一清洗文本并转为小写，便于关键词匹配。
     */
    private String normalize(String value) {
        String trimmed = trimToNull(value);
        return trimmed == null ? null : trimmed.toLowerCase();
    }

    /**
     * 去除字符串首尾空白，并将空字符串转换为 null。
     */
    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
