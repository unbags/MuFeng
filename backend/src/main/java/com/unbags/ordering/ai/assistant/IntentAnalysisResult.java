package com.unbags.ordering.ai.assistant;

import java.util.ArrayList;
import java.util.List;

public class IntentAnalysisResult {

    private AssistantIntent intent;
    private double confidence;
    private List<Item> items = new ArrayList<>();
    private SafetyFlags safetyFlags = new SafetyFlags();
    private boolean needsClarification;
    private String clarificationReason;

    public AssistantIntent getIntent() {
        return intent;
    }

    public void setIntent(AssistantIntent intent) {
        this.intent = intent;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public SafetyFlags getSafetyFlags() {
        return safetyFlags;
    }

    public void setSafetyFlags(SafetyFlags safetyFlags) {
        this.safetyFlags = safetyFlags;
    }

    public boolean isNeedsClarification() {
        return needsClarification;
    }

    public void setNeedsClarification(boolean needsClarification) {
        this.needsClarification = needsClarification;
    }

    public String getClarificationReason() {
        return clarificationReason;
    }

    public void setClarificationReason(String clarificationReason) {
        this.clarificationReason = clarificationReason;
    }

    public static class Item {
        private String rawName;
        private Integer quantity;
        private String unit;
        private String remark;

        public Item() {
        }

        public Item(String rawName, Integer quantity, String unit, String remark) {
            this.rawName = rawName;
            this.quantity = quantity;
            this.unit = unit;
            this.remark = remark;
        }

        public String getRawName() {
            return rawName;
        }

        public void setRawName(String rawName) {
            this.rawName = rawName;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }
    }

    public static class SafetyFlags {
        private boolean requestsCreateOrder;
        private boolean requestsPayment;
        private boolean requestsAmountConfirmation;

        public boolean isRequestsCreateOrder() {
            return requestsCreateOrder;
        }

        public void setRequestsCreateOrder(boolean requestsCreateOrder) {
            this.requestsCreateOrder = requestsCreateOrder;
        }

        public boolean isRequestsPayment() {
            return requestsPayment;
        }

        public void setRequestsPayment(boolean requestsPayment) {
            this.requestsPayment = requestsPayment;
        }

        public boolean isRequestsAmountConfirmation() {
            return requestsAmountConfirmation;
        }

        public void setRequestsAmountConfirmation(boolean requestsAmountConfirmation) {
            this.requestsAmountConfirmation = requestsAmountConfirmation;
        }

        public boolean hasTransactionRisk() {
            return requestsCreateOrder || requestsPayment || requestsAmountConfirmation;
        }
    }
}
