package com.crm.service;

import com.crm.dto.transaction.TransactionCreateDto;
import com.crm.dto.transaction.TransactionResponseDto;
import com.crm.entity.Seller;
import com.crm.entity.Transaction;
import com.crm.enums.PaymentType;
import com.crm.exception.NotFoundException;
import com.crm.mapper.TransactionMapper;
import com.crm.repository.SellerRepository;
import com.crm.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private SellerRepository sellerRepository;
    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private TransactionService transactionService;

    private Seller seller;
    private Transaction transaction;
    private TransactionResponseDto responseDto;

    @BeforeEach
    void setUp() {
        seller = new Seller();
        seller.setId(1L);
        seller.setName("Тестовый продавец");

        transaction = new Transaction();
        transaction.setId(1L);
        transaction.setSeller(seller);
        transaction.setAmount(new BigDecimal("500.00"));
        transaction.setPaymentType(PaymentType.CARD);
        transaction.setTransactionDate(LocalDateTime.now());

        responseDto = TransactionResponseDto.builder()
                .id(1L)
                .sellerId(1L)
                .sellerName("Тестовый продавец")
                .amount(new BigDecimal("500.00"))
                .paymentType(PaymentType.CARD)
                .transactionDate(transaction.getTransactionDate())
                .build();
    }

    @Test
    void getAllTransactions_shouldReturnList() {
        when(transactionRepository.findAll()).thenReturn(List.of(transaction));
        when(transactionMapper.toDto(transaction)).thenReturn(responseDto);

        List<TransactionResponseDto> result = transactionService.getAllTransactions();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAmount()).isEqualByComparingTo("500.00");
    }

    @Test
    void getTransactionById_shouldReturn_whenExists() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(transactionMapper.toDto(transaction)).thenReturn(responseDto);

        TransactionResponseDto result = transactionService.getTransactionById(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getTransactionById_shouldThrow_whenNotFound() {
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.getTransactionById(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void createTransaction_shouldSave_whenSellerExists() {
        TransactionCreateDto createDto = new TransactionCreateDto();
        createDto.setSellerId(1L);
        createDto.setAmount(new BigDecimal("999.99"));
        createDto.setPaymentType(PaymentType.CASH);

        when(sellerRepository.findActiveById(1L)).thenReturn(Optional.of(seller));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(transactionMapper.toDto(transaction)).thenReturn(responseDto);

        TransactionResponseDto result = transactionService.createTransaction(createDto);

        assertThat(result).isNotNull();
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void createTransaction_shouldSetCurrentTime_whenDateNotProvided() {
        TransactionCreateDto createDto = new TransactionCreateDto();
        createDto.setSellerId(1L);
        createDto.setAmount(BigDecimal.TEN);
        createDto.setPaymentType(PaymentType.TRANSFER);

        when(sellerRepository.findActiveById(1L)).thenReturn(Optional.of(seller));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction saved = inv.getArgument(0);
            assertThat(saved.getTransactionDate()).isNotNull();
            return saved;
        });
        when(transactionMapper.toDto(any())).thenReturn(responseDto);

        transactionService.createTransaction(createDto);

        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void createTransaction_shouldThrow_whenSellerNotFound() {
        TransactionCreateDto createDto = new TransactionCreateDto();
        createDto.setSellerId(99L);
        createDto.setAmount(BigDecimal.TEN);
        createDto.setPaymentType(PaymentType.CASH);

        when(sellerRepository.findActiveById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.createTransaction(createDto))
                .isInstanceOf(NotFoundException.class);

        verify(transactionRepository, never()).save(any());
    }
}