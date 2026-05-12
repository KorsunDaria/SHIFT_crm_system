package com.crm.service;

import com.crm.dto.transaction.TransactionCreateDto;
import com.crm.dto.transaction.TransactionResponseDto;
import com.crm.entity.Seller;
import com.crm.entity.Transaction;
import com.crm.exception.NotFoundException;
import com.crm.mapper.TransactionMapper;
import com.crm.repository.SellerRepository;
import com.crm.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final SellerRepository sellerRepository;
    private final TransactionMapper transactionMapper;

    public TransactionService(TransactionRepository transactionRepository,
                              SellerRepository sellerRepository,
                              TransactionMapper transactionMapper) {
        this.transactionRepository = transactionRepository;
        this.sellerRepository = sellerRepository;
        this.transactionMapper = transactionMapper;
    }

    @Transactional(readOnly = true)
    public List<TransactionResponseDto> getAllTransactions() {
        return transactionRepository.findAll()
                .stream()
                .map(transactionMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public TransactionResponseDto getTransactionById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> NotFoundException.transaction(id));
        return transactionMapper.toDto(transaction);
    }

    @Transactional
    public TransactionResponseDto createTransaction(TransactionCreateDto dto) {
        Seller seller = sellerRepository.findActiveById(dto.getSellerId())
                .orElseThrow(() -> NotFoundException.seller(dto.getSellerId()));

        Transaction transaction = new Transaction();
        transaction.setSeller(seller);
        transaction.setAmount(dto.getAmount());
        transaction.setPaymentType(dto.getPaymentType());
        transaction.setTransactionDate(
                dto.getTransactionDate() != null ? dto.getTransactionDate() : LocalDateTime.now()
        );

        return transactionMapper.toDto(transactionRepository.save(transaction));
    }
}