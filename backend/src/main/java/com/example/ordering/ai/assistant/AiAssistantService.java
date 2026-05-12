package com.example.ordering.ai.assistant;

import com.example.ordering.ai.prompt.PromptTemplateService;
import com.example.ordering.dto.ChatRequest;
import com.example.ordering.dto.ChatResponse;
import com.example.ordering.dto.DishResponse;
import com.example.ordering.dto.MenuResponse;
import com.example.ordering.dto.OrderDetailResponse;
import com.example.ordering.service.ChatService;
import com.example.ordering.service.MenuService;
import com.example.ordering.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AiAssistantService {

    private static final Logger log = LoggerFactory.getLogger(AiAssistantService.class);

    private static final String DEFAULT_CONVERSATION_ID = "default";

    private final AiAssistantProperties properties;
    private final PromptTemplateService promptTemplateService;
    private final ChatService fallback;
    private final ChatClient chatClient;
    private final QuestionAnswerAdvisor ragAdvisor;
    private final MenuService menuService;
    private final OrderService orderService;

    public AiAssistantService(AiAssistantProperties properties,
                               PromptTemplateService promptTemplateService,
                               ChatService fallback,
                               ObjectProvider<ChatClient> chatClientProvider,
                               ObjectProvider<QuestionAnswerAdvisor> ragAdvisorProvider,
                               MenuService menuService,
                               OrderService orderService) {
        this.properties = properties;
        this.promptTemplateService = promptTemplateService;
        this.fallback = fallback;
        this.chatClient = chatClientProvider.getIfAvailable();
        this.ragAdvisor = ragAdvisorProvider.getIfAvailable();
        this.menuService = menuService;
        this.orderService = orderService;
    }

    public AssistantMode mode() {
        return properties.resolveMode();
    }

    public AssistantRoute route(ChatRequest request) {
        if (mode() == AssistantMode.RULE_BASED) {
            return AssistantRoute.RULE_BASED;
        }
        String orderNo = normalize(request == null ? null : request.getOrderNo());
        if (orderNo != null) {
            return AssistantRoute.TOOL_FIRST;
        }
        String message = normalize(request == null ? null : request.getMessage());
        if (message != null && containsAny(message,
                "营业", "时间", "开门", "关门", "几点",
                "支付", "付款", "退款", "怎么退",
                "外带", "取餐", "怎么取", "取餐号",
                "电话", "地址", "位置", "在哪",
                "预订", "包厢")) {
            return AssistantRoute.RAG_FIRST;
        }
        return AssistantRoute.TOOL_FIRST;
    }

    public String customerSystemPrompt() {
        return promptTemplateService.render("customer-system", Collections.emptyMap());
    }

    /**
     * 一次性回复。先根据意图预取实时数据注入到消息中，再由 LLM 生成自然语言回复。
     * 完全绕过 DeepSeek 函数调用机制，避免兼容性问题。
     */
    public ChatResponse reply(ChatRequest request) {
        if (chatClient == null || mode() == AssistantMode.RULE_BASED) {
            return fallback.reply(request);
        }

        AssistantRoute currentRoute = route(request);
        String userMessage = buildUserMessage(request);
        String systemPrompt = customerSystemPrompt();
        String conversationId = conversationId(request);

        String enrichedMessage = enrichWithData(userMessage, request);

        try {
            log.info("AI request: convId={} route={} msgLen={}", conversationId, currentRoute, enrichedMessage.length());
            String answer = chatClient.prompt()
                .user(enrichedMessage)
                .system(systemPrompt)
                .advisors(a -> {
                    a.param(ChatMemory.CONVERSATION_ID, conversationId);
                    if (currentRoute == AssistantRoute.RAG_FIRST && ragAdvisor != null) {
                        a.advisors(ragAdvisor);
                    }
                })
                .call()
                .content();
            String normalizedAnswer = trimToNull(answer);
            if (normalizedAnswer == null) {
                log.warn("AI returned blank, falling back; convId={}", conversationId);
                return fallback.reply(request);
            }
            log.info("AI reply: convId={} route={} source={} answerLen={}",
                conversationId, currentRoute, sourceTag(), normalizedAnswer.length());
            return new ChatResponse(normalizedAnswer, sourceTag(), LocalDateTime.now());
        } catch (Exception e) {
            log.error("AI failed, falling back; convId={} route={}",
                conversationId, currentRoute, e);
            return fallback.reply(request);
        }
    }

    /**
     * 预取实时数据并拼接到用户消息中，让 LLM 无需调用函数即可获得数据。
     */
    private String enrichWithData(String userMessage, ChatRequest request) {
        StringBuilder ctx = new StringBuilder();

        String orderNo = trimToNull(request == null ? null : request.getOrderNo());
        String rawMessage = trimToNull(request == null ? null : request.getMessage());

        // 订单查询：预取订单数据
        if (orderNo != null) {
            try {
                OrderDetailResponse order = orderService.getOrder(orderNo);
                ctx.append("[订单实时数据]\n");
                ctx.append("订单号: ").append(order.getOrderNo()).append("\n");
                ctx.append("状态: ").append(order.getStatus()).append("\n");
                ctx.append("菜品: ");
                if (order.getItems() != null && !order.getItems().isEmpty()) {
                    ctx.append(order.getItems().stream()
                        .map(i -> i.getName() + " x" + i.getQuantity())
                        .collect(Collectors.joining("、")));
                } else {
                    ctx.append("无");
                }
                ctx.append("\n总金额: ").append(order.getTotalAmount()).append("元\n\n");
            } catch (Exception e) {
                log.warn("Failed to fetch order data for orderNo={}", orderNo, e);
            }
        }

        // 菜单/推荐查询：预取菜单数据
        boolean needsMenu = rawMessage != null && containsAny(rawMessage,
            "菜单", "推荐", "好吃", "招牌", "特色", "有什么", "吃点", "能吃", "想吃", "来点",
            "什么菜", "哪些菜", "有没有", "找一下", "看看", "介绍", "价格", "多少钱",
            "素", "肉", "辣", "清淡", "甜", "酸", "汤", "面", "饭",
            "饮料", "喝", "小吃", "主食", "菜", "dish", "menu");
        boolean isGreeting = rawMessage == null || rawMessage.length() <= 3
            || rawMessage.matches("^(你好|嗨|hi|hello|您好|在吗|在不在).*");

        if (needsMenu && !isGreeting) {
            try {
                MenuResponse menu = menuService.getMenu();
                List<DishResponse> dishes = menu.getDishes();
                if (dishes != null && !dishes.isEmpty()) {
                    ctx.append("[今日实时菜单]\n");
                    for (DishResponse d : dishes) {
                        ctx.append("- ").append(d.getName())
                            .append(" | ").append(d.getCategory() != null ? d.getCategory() : "-")
                            .append(" | ").append(d.getPrice() != null ? d.getPrice() + "元" : "-");
                        if (d.getHighlight() != null && !d.getHighlight().trim().isEmpty()) {
                            ctx.append(" | ").append(d.getHighlight());
                        }
                        ctx.append("\n");
                    }
                    ctx.append("\n");
                }
            } catch (Exception e) {
                log.warn("Failed to fetch menu data", e);
            }
        }

        if (ctx.length() > 0) {
            ctx.append("用户问题: ").append(userMessage);
            return ctx.toString();
        }
        return userMessage;
    }

    /**
     * 流式回复（SSE），AI 不可用时回退到规则回复。
     */
    public Flux<String> stream(ChatRequest request) {
        if (chatClient == null || mode() == AssistantMode.RULE_BASED) {
            ChatResponse reply = fallback.reply(request);
            return Flux.just(reply.getMessage());
        }

        AssistantRoute currentRoute = route(request);
        String userMessage = buildUserMessage(request);
        String systemPrompt = customerSystemPrompt();
        String conversationId = conversationId(request);

        String enrichedMessage = enrichWithData(userMessage, request);

        log.info("AI stream started; convId={} route={}", conversationId, currentRoute);

        try {
            return chatClient.prompt()
                .user(enrichedMessage)
                .system(systemPrompt)
                .advisors(a -> {
                    a.param(ChatMemory.CONVERSATION_ID, conversationId);
                    if (currentRoute == AssistantRoute.RAG_FIRST && ragAdvisor != null) {
                        a.advisors(ragAdvisor);
                    }
                })
                .stream()
                .content()
                .filter(chunk -> trimToNull(chunk) != null)
                .switchIfEmpty(Flux.defer(() -> {
                    log.warn("AI stream empty, falling back; convId={}", conversationId);
                    ChatResponse reply = fallback.reply(request);
                    return Flux.just(reply.getMessage());
                }))
                .onErrorResume(e -> {
                    log.error("AI stream failed, falling back; convId={}", conversationId, e);
                    ChatResponse reply = fallback.reply(request);
                    return Flux.just(reply.getMessage());
                });
        } catch (Exception e) {
            log.error("AI stream setup failed, falling back; convId={}", conversationId, e);
            ChatResponse reply = fallback.reply(request);
            return Flux.just(reply.getMessage());
        }
    }

    private String buildUserMessage(ChatRequest request) {
        StringBuilder sb = new StringBuilder();
        String orderNo = trimToNull(request.getOrderNo());
        String message = trimToNull(request.getMessage());

        if (orderNo != null) {
            sb.append("订单号: ").append(orderNo).append("。");
        }
        if (message != null) {
            sb.append(" ").append(message);
        }
        if (sb.length() == 0) {
            sb.append("你好");
        }
        return sb.toString().trim();
    }

    private String conversationId(ChatRequest request) {
        if (request == null) return DEFAULT_CONVERSATION_ID;
        String id = trimToNull(request.getConversationId());
        return id != null ? id : DEFAULT_CONVERSATION_ID;
    }

    private String sourceTag() {
        AssistantMode m = mode();
        if (m == AssistantMode.FULL_AI || m == AssistantMode.LLM_ONLY) return "AI";
        if (m == AssistantMode.RAG_ONLY) return "AI-RAG";
        return "RULE";
    }

    private boolean containsAny(String value, String... keywords) {
        if (value == null) return false;
        for (String kw : keywords) {
            if (value.contains(kw.toLowerCase())) return true;
        }
        return false;
    }

    private String normalize(String value) {
        String trimmed = trimToNull(value);
        return trimmed == null ? null : trimmed.toLowerCase();
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
