package com.example.ordering.ai.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class BusinessRuleTools {

    private final BigDecimal packageFee;
    private final BigDecimal deliveryFee;

    public BusinessRuleTools(
        @Value("${app.order.package-fee:2.00}") BigDecimal packageFee,
        @Value("${app.order.delivery-fee:4.00}") BigDecimal deliveryFee
    ) {
        this.packageFee = packageFee;
        this.deliveryFee = deliveryFee;
    }

    /**
     * 查询点餐业务规则和费用配置，供 AI 助手回答规则问题。
     */
    @Tool(description = "查询点餐业务规则，包括打包费、配送费、取餐和订单状态说明。")
    public BusinessRuleToolResponse getBusinessRules() {
        return new BusinessRuleToolResponse(
            packageFee,
            deliveryFee,
            "订单 READY 后可准备取餐；具体取餐与价格信息以页面实时展示为准。"
        );
    }
}
