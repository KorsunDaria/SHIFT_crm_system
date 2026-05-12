package com.crm.dto.analytics;

import java.math.BigDecimal;

public class LowPerformerSellerDto {

    private Long sellerId;
    private String sellerName;
    private BigDecimal totalAmount;

    public Long getSellerId() { return sellerId; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }

    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
}