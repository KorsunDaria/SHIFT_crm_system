package com.crm.dto.transaction;

import com.crm.enums.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionResponseDto {

    private Long id;
    private Long sellerId;
    private String sellerName;
    private BigDecimal amount;
    private PaymentType paymentType;
    private LocalDateTime transactionDate;

    private TransactionResponseDto() {}

    public static Builder builder() { return new Builder(); }

    public Long getId() { return id; }
    public Long getSellerId() { return sellerId; }
    public String getSellerName() { return sellerName; }
    public BigDecimal getAmount() { return amount; }
    public PaymentType getPaymentType() { return paymentType; }
    public LocalDateTime getTransactionDate() { return transactionDate; }

    public static class Builder {
        private final TransactionResponseDto dto = new TransactionResponseDto();

        public Builder id(Long id) { dto.id = id; return this; }
        public Builder sellerId(Long sellerId) { dto.sellerId = sellerId; return this; }
        public Builder sellerName(String sellerName) { dto.sellerName = sellerName; return this; }
        public Builder amount(BigDecimal amount) { dto.amount = amount; return this; }
        public Builder paymentType(PaymentType paymentType) { dto.paymentType = paymentType; return this; }
        public Builder transactionDate(LocalDateTime transactionDate) { dto.transactionDate = transactionDate; return this; }

        public TransactionResponseDto build() { return dto; }
    }
}