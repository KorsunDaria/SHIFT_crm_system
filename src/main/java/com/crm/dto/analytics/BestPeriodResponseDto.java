package com.crm.dto.analytics;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BestPeriodResponseDto {

    private Long sellerId;
    private String sellerName;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private int transactionCount;
    private BigDecimal totalAmount;

    private BestPeriodResponseDto() {}

    public static Builder builder() { return new Builder(); }

    public Long getSellerId() { return sellerId; }
    public String getSellerName() { return sellerName; }
    public LocalDateTime getPeriodStart() { return periodStart; }
    public LocalDateTime getPeriodEnd() { return periodEnd; }
    public int getTransactionCount() { return transactionCount; }
    public BigDecimal getTotalAmount() { return totalAmount; }

    public static class Builder {
        private final BestPeriodResponseDto dto = new BestPeriodResponseDto();

        public Builder sellerId(Long sellerId) { dto.sellerId = sellerId; return this; }
        public Builder sellerName(String sellerName) { dto.sellerName = sellerName; return this; }
        public Builder periodStart(LocalDateTime periodStart) { dto.periodStart = periodStart; return this; }
        public Builder periodEnd(LocalDateTime periodEnd) { dto.periodEnd = periodEnd; return this; }
        public Builder transactionCount(int transactionCount) { dto.transactionCount = transactionCount; return this; }
        public Builder totalAmount(BigDecimal totalAmount) { dto.totalAmount = totalAmount; return this; }

        public BestPeriodResponseDto build() { return dto; }
    }
}