package com.crm.mapper;

import com.crm.dto.transaction.TransactionResponseDto;
import com.crm.entity.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public TransactionResponseDto toDto(Transaction t) {
        return TransactionResponseDto.builder()
                .id(t.getId())
                .sellerId(t.getSeller().getId())
                .sellerName(t.getSeller().getName())
                .amount(t.getAmount())
                .paymentType(t.getPaymentType())
                .transactionDate(t.getTransactionDate())
                .build();
    }
}