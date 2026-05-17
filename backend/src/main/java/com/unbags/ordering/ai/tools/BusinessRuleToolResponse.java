package com.unbags.ordering.ai.tools;

import java.math.BigDecimal;

public class BusinessRuleToolResponse {

    private BigDecimal serviceFee;
    private String pickupRule;

    public BusinessRuleToolResponse() {
    }

    public BusinessRuleToolResponse(BigDecimal serviceFee, String pickupRule) {
        this.serviceFee = serviceFee;
        this.pickupRule = pickupRule;
    }

    public BigDecimal serviceFee() {
        return serviceFee;
    }

    public String pickupRule() {
        return pickupRule;
    }

    public BigDecimal getServiceFee() {
        return serviceFee;
    }

    public void setServiceFee(BigDecimal serviceFee) {
        this.serviceFee = serviceFee;
    }

    public String getPickupRule() {
        return pickupRule;
    }

    public void setPickupRule(String pickupRule) {
        this.pickupRule = pickupRule;
    }
}
