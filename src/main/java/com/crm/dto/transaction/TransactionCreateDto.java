package com.crm.dto.transaction;

import com.crm.enums.PaymentType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionCreateDto {

    @NotNull(message = "ID продавца обязателен")
    private Long sellerId;

    @NotNull(message = "Сумма транзакции обязательна")
    @Positive(message = "Сумма должна быть положительной")
    private BigDecimal amount;

    @NotNull(message = "Тип оплаты обязателен")
    private PaymentType paymentType;

    private LocalDateTime transactionDate;

    public Long getSellerId() { return sellerId; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public PaymentType getPaymentType() { return paymentType; }
    public void setPaymentType(PaymentType paymentType) { this.paymentType = paymentType; }

    public LocalDateTime getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDateTime transactionDate) { this.transactionDate = transactionDate; }
}