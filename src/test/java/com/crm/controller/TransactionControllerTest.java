package com.crm.controller;

import com.crm.dto.transaction.TransactionCreateDto;
import com.crm.dto.transaction.TransactionResponseDto;
import com.crm.enums.PaymentType;
import com.crm.exception.GlobalExceptionHandler;
import com.crm.exception.NotFoundException;
import com.crm.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionController transactionController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private TransactionResponseDto txResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(transactionController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        txResponse = TransactionResponseDto.builder()
                .id(1L)
                .sellerId(1L)
                .sellerName("Тест продавец")
                .amount(new BigDecimal("2500.00"))
                .paymentType(PaymentType.CARD)
                .transactionDate(LocalDateTime.of(2026, 6, 10, 15, 0))
                .build();
    }

    @Test
    void getAllTransactions_shouldReturn200WithList() throws Exception {
        when(transactionService.getAllTransactions()).thenReturn(List.of(txResponse));

        mockMvc.perform(get("/api/v1/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].amount").value(2500.00))
                .andExpect(jsonPath("$[0].paymentType").value("CARD"));
    }

    @Test
    void getAllTransactions_shouldReturnEmptyList() throws Exception {
        when(transactionService.getAllTransactions()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getTransactionById_shouldReturn200_whenExists() throws Exception {
        when(transactionService.getTransactionById(1L)).thenReturn(txResponse);

        mockMvc.perform(get("/api/v1/transactions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.sellerName").value("Тест продавец"));
    }

    @Test
    void getTransactionById_shouldReturn404_whenNotFound() throws Exception {
        when(transactionService.getTransactionById(99L))
                .thenThrow(NotFoundException.transaction(99L));

        mockMvc.perform(get("/api/v1/transactions/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void createTransaction_shouldReturn201_withValidBody() throws Exception {
        TransactionCreateDto createDto = new TransactionCreateDto();
        createDto.setSellerId(1L);
        createDto.setAmount(new BigDecimal("999.99"));
        createDto.setPaymentType(PaymentType.CASH);

        when(transactionService.createTransaction(any())).thenReturn(txResponse);

        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void createTransaction_shouldReturn400_whenSellerIdMissing() throws Exception {
        TransactionCreateDto createDto = new TransactionCreateDto();
        createDto.setAmount(new BigDecimal("100.00"));
        createDto.setPaymentType(PaymentType.TRANSFER);

        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTransaction_shouldReturn400_whenAmountIsNegative() throws Exception {
        TransactionCreateDto createDto = new TransactionCreateDto();
        createDto.setSellerId(1L);
        createDto.setAmount(new BigDecimal("-100.00"));
        createDto.setPaymentType(PaymentType.CASH);

        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTransaction_shouldReturn400_whenPaymentTypeMissing() throws Exception {
        TransactionCreateDto createDto = new TransactionCreateDto();
        createDto.setSellerId(1L);
        createDto.setAmount(new BigDecimal("500.00"));

        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTransaction_shouldReturn404_whenSellerNotFound() throws Exception {
        TransactionCreateDto createDto = new TransactionCreateDto();
        createDto.setSellerId(99L);
        createDto.setAmount(new BigDecimal("100.00"));
        createDto.setPaymentType(PaymentType.CARD);

        when(transactionService.createTransaction(any()))
                .thenThrow(NotFoundException.seller(99L));

        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isNotFound());
    }
}
