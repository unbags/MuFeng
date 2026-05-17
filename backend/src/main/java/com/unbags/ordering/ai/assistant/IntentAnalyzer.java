package com.unbags.ordering.ai.assistant;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class IntentAnalyzer {

    private static final Pattern QUANTITY_PATTERN = Pattern.compile("([0-9]+|[一二两三四五六七八九十])\\s*(份|个|杯|碗|盒)?");

    public IntentAnalysisResult analyze(String userMessage) {
        String message = normalize(userMessage);
        IntentAnalysisResult result = new IntentAnalysisResult();
        IntentAnalysisResult.SafetyFlags flags = new IntentAnalysisResult.SafetyFlags();
        flags.setRequestsCreateOrder(containsAny(message, "下单", "提交订单", "创建订单", "结账"));
        flags.setRequestsPayment(containsAny(message, "付款", "支付", "买单"));
        flags.setRequestsAmountConfirmation(containsAny(message, "确认金额", "确认交易"));
        result.setSafetyFlags(flags);

        if (message == null) {
            result.setIntent(AssistantIntent.CHITCHAT);
            result.setConfidence(0.5d);
            result.setNeedsClarification(false);
            return result;
        }

        if (containsAny(message, "购物车", "点餐车") && containsAny(message, "有什么", "查看", "看看", "查询")) {
            result.setIntent(AssistantIntent.CART_QUERY);
            result.setConfidence(0.9d);
            return result;
        }
        if (containsAny(message, "不要", "去掉", "移除", "删除")) {
            result.setIntent(AssistantIntent.CART_REMOVE);
            result.setConfidence(0.82d);
            return result;
        }
        if (containsAny(message, "改成", "换成", "多加", "少来")) {
            result.setIntent(AssistantIntent.CART_MODIFY);
            result.setConfidence(0.82d);
            return result;
        }
        if (containsAny(message, "订单", "ORD") && containsAny(message, "查", "状态", "到哪", "进度")) {
            result.setIntent(AssistantIntent.ORDER_QUERY);
            result.setConfidence(0.88d);
            return result;
        }
        if (containsAny(message, "推荐", "好吃", "吃什么", "有什么", "招牌菜", "今日招牌", "今日推荐")) {
            result.setIntent(AssistantIntent.RECOMMENDATION);
            result.setConfidence(0.86d);
            return result;
        }

        List<IntentAnalysisResult.Item> items = extractItems(message);
        if (!items.isEmpty()) {
            result.setItems(items);
            boolean quantityMissing = items.stream().anyMatch(item -> item.getQuantity() == null);
            result.setIntent(quantityMissing ? AssistantIntent.AMBIGUOUS_ORDER : AssistantIntent.EXPLICIT_ORDER);
            result.setConfidence(quantityMissing ? 0.78d : 0.92d);
            result.setNeedsClarification(quantityMissing);
            result.setClarificationReason(quantityMissing ? "数量缺失" : null);
            return result;
        }

        if (flags.hasTransactionRisk()) {
            result.setIntent(AssistantIntent.TRANSACTION);
            result.setConfidence(0.9d);
            result.setNeedsClarification(true);
            result.setClarificationReason("交易类请求不能由 AI 代操作");
            return result;
        }
        if (containsAny(message, "辣", "口味", "配料", "价格", "多少钱", "介绍", "还有吗")) {
            result.setIntent(AssistantIntent.DISH_INQUIRY);
            result.setConfidence(0.78d);
            return result;
        }

        result.setIntent(AssistantIntent.CHITCHAT);
        result.setConfidence(0.55d);
        return result;
    }

    private List<IntentAnalysisResult.Item> extractItems(String message) {
        if (message == null) {
            return List.of();
        }
        String candidate = message
            .replaceAll("帮我下单并付款|帮我下单|提交订单|创建订单|并付款|付款|支付|结账", "")
            .replaceAll(orderingPrefixPattern(), "")
            .trim();
        if (candidate.isEmpty()) {
            return List.of();
        }

        List<IntentAnalysisResult.Item> items = new ArrayList<>();
        for (String part : candidate.split("[、，,；;和]")) {
            IntentAnalysisResult.Item item = parseItem(part);
            if (item != null) {
                items.add(item);
            }
        }
        return items;
    }

    private IntentAnalysisResult.Item parseItem(String value) {
        String candidate = normalize(value);
        if (candidate == null) {
            return null;
        }
        candidate = candidate.replaceAll(orderingPrefixPattern(), "").trim();
        Matcher matcher = QUANTITY_PATTERN.matcher(candidate);
        Integer quantity = null;
        String unit = null;
        if (matcher.find()) {
            quantity = parseQuantity(matcher.group(1));
            unit = matcher.group(2);
            candidate = (candidate.substring(0, matcher.start()) + candidate.substring(matcher.end())).trim();
        }
        candidate = candidate
            .replaceAll(orderingPrefixPattern(), "")
            .replaceAll("^(一份|一杯|一个|一碗|一盒)", "")
            .replaceAll(orderingPrefixPattern(), "")
            .trim();
        candidate = candidate.replaceAll("[，。,.!！?？]$", "").trim();
        if (candidate.length() < 2 || containsAny(candidate, "下单", "付款", "支付")) {
            return null;
        }
        return new IntentAnalysisResult.Item(candidate, quantity, unit, null);
    }

    private String orderingPrefixPattern() {
        return "^(我想点|想点|单点|我想要|我要|帮我点|帮我下|帮我来|给我来|给我点|再来|再点|再要|再加|来|点|要)";
    }

    private Integer parseQuantity(String value) {
        if (value == null) {
            return null;
        }
        if (value.matches("[0-9]+")) {
            return Integer.parseInt(value);
        }
        return switch (value) {
            case "一" -> 1;
            case "二", "两" -> 2;
            case "三" -> 3;
            case "四" -> 4;
            case "五" -> 5;
            case "六" -> 6;
            case "七" -> 7;
            case "八" -> 8;
            case "九" -> 9;
            case "十" -> 10;
            default -> null;
        };
    }

    private boolean containsAny(String value, String... keywords) {
        if (value == null) {
            return false;
        }
        for (String keyword : keywords) {
            if (value.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
