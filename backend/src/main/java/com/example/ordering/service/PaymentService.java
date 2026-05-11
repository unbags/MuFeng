package com.example.ordering.service;

import java.math.BigDecimal;

public interface PaymentService {

    /** 发起支付并返回支付结果。 */
    PaymentResult pay(String orderNo, BigDecimal amount, String method);

    /** 发起退款并返回退款结果。 */
    PaymentResult refund(String orderNo, BigDecimal amount);

    class PaymentResult {
        private final boolean success;
        private final String transactionId;
        private final String message;

        public PaymentResult(boolean success, String transactionId, String message) {
            this.success = success;
            this.transactionId = transactionId;
            this.message = message;
        }

        /** 返回支付或退款是否成功。 */
        public boolean isSuccess() { return success; }
        /** 返回支付或退款流水号。 */
        public String getTransactionId() { return transactionId; }
        /** 返回支付或退款提示信息。 */
        public String getMessage() { return message; }
    }
}
