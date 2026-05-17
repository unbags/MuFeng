package com.example.ordering.ai.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class BusinessRuleTools {

    private final ToolAuditSupport auditSupport;

    public BusinessRuleTools() {
        this(ToolAuditSupport.disabled());
    }

    @Autowired
    public BusinessRuleTools(ToolAuditSupport auditSupport) {
        this.auditSupport = auditSupport;
    }

    /**
     * 查询点餐业务规则和费用配置，供 AI 助手回答规则问题。
     */
    @Tool(description = "Get business rules: pickup methods, payment options, business hours, order status explanations, and refund policy. Call when user asks about these topics. 查询取餐/支付/营业时间/退款等业务规则。")
    public BusinessRuleToolResponse getBusinessRules() {
        return auditSupport.record("getBusinessRules", "all", () ->
            new BusinessRuleToolResponse(
                BigDecimal.ZERO,
                "堂食请到店用餐，外带到店自取出餐号无额外费用。支持微信支付和支付宝在线支付，也可到店现金或刷卡。午餐11:00-14:00，晚餐17:00-21:30。订单提交后无法在线修改或退款，需联系店员处理。具体信息以页面实时展示为准。"
            ));
    }
}
