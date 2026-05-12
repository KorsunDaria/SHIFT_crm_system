package com.crm.dto.analytics;

import java.math.BigDecimal;

public class BestSellerResponseDto {

    private Long sellerId;
    private String sellerName;
    private BigDecimal totalAmount;
    private String period;

    private BestSellerResponseDto() {}

    public static Builder builder() { return new Builder(); }

    public Long getSellerId() { return sellerId; }
    public String getSellerName() { return sellerName; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getPeriod() { return period; }

    public static class Builder {
        private final BestSellerResponseDto dto = new BestSellerResponseDto();

        public Builder sellerId(Long sellerId) { dto.sellerId = sellerId; return this; }
        public Builder sellerName(String sellerName) { dto.sellerName = sellerName; return this; }
        public Builder totalAmount(BigDecimal totalAmount) { dto.totalAmount = totalAmount; return this; }
        public Builder period(String period) { dto.period = period; return this; }

        public BestSellerResponseDto build() { return dto; }
    }
}