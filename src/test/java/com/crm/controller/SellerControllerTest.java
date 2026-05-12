package com.crm.controller;

import com.crm.dto.seller.SellerCreateDto;
import com.crm.dto.seller.SellerResponseDto;
import com.crm.dto.seller.SellerUpdateDto;
import com.crm.dto.transaction.TransactionResponseDto;
import com.crm.enums.PaymentType;
import com.crm.exception.GlobalExceptionHandler;
import com.crm.exception.NotFoundException;
import com.crm.service.SellerService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class SellerControllerTest {

    @Mock
    private SellerService sellerService;

    @InjectMocks
    private SellerController sellerController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private SellerResponseDto sellerResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(sellerController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        sellerResponse = SellerResponseDto.builder()
                .id(1L)
                .name("Иван Иванов")
                .contactInfo("ivan@mail.ru")
                .registrationDate(LocalDateTime.of(2026, 1, 15, 10, 0))
                .build();
    }

    @Test
    void getAllSellers_shouldReturn200WithList() throws Exception {
        when(sellerService.getAllSellers()).thenReturn(List.of(sellerResponse));

        mockMvc.perform(get("/api/v1/sellers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Иван Иванов"));
    }

    @Test
    void getAllSellers_shouldReturnEmptyList_whenNoSellers() throws Exception {
        when(sellerService.getAllSellers()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/sellers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getSellerById_shouldReturn200_whenExists() throws Exception {
        when(sellerService.getSellerById(1L)).thenReturn(sellerResponse);

        mockMvc.perform(get("/api/v1/sellers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Иван Иванов"))
                .andExpect(jsonPath("$.contactInfo").value("ivan@mail.ru"));
    }

    @Test
    void getSellerById_shouldReturn404_whenNotFound() throws Exception {
        when(sellerService.getSellerById(99L)).thenThrow(NotFoundException.seller(99L));

        mockMvc.perform(get("/api/v1/sellers/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("99")));
    }

    @Test
    void createSeller_shouldReturn201_withValidBody() throws Exception {
        SellerCreateDto createDto = new SellerCreateDto();
        createDto.setName("Новый продавец");
        createDto.setContactInfo("new@mail.ru");

        when(sellerService.createSeller(any())).thenReturn(sellerResponse);

        mockMvc.perform(post("/api/v1/sellers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void createSeller_shouldReturn400_whenNameIsBlank() throws Exception {
        SellerCreateDto createDto = new SellerCreateDto();
        createDto.setName("  ");

        mockMvc.perform(post("/api/v1/sellers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createSeller_shouldReturn400_whenNameIsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/sellers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateSeller_shouldReturn200_withValidBody() throws Exception {
        SellerUpdateDto updateDto = new SellerUpdateDto();
        updateDto.setName("Обновлённый");

        when(sellerService.updateSeller(eq(1L), any())).thenReturn(sellerResponse);

        mockMvc.perform(put("/api/v1/sellers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk());
    }

    @Test
    void updateSeller_shouldReturn404_whenSellerNotFound() throws Exception {
        SellerUpdateDto updateDto = new SellerUpdateDto();
        updateDto.setName("Имя");

        when(sellerService.updateSeller(eq(99L), any())).thenThrow(NotFoundException.seller(99L));

        mockMvc.perform(put("/api/v1/sellers/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteSeller_shouldReturn204() throws Exception {
        doNothing().when(sellerService).deleteSeller(1L);

        mockMvc.perform(delete("/api/v1/sellers/1"))
                .andExpect(status().isNoContent());

        verify(sellerService).deleteSeller(1L);
    }

    @Test
    void deleteSeller_shouldReturn404_whenNotFound() throws Exception {
        doThrow(NotFoundException.seller(99L)).when(sellerService).deleteSeller(99L);

        mockMvc.perform(delete("/api/v1/sellers/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getSellerTransactions_shouldReturn200WithList() throws Exception {
        TransactionResponseDto txDto = TransactionResponseDto.builder()
                .id(10L)
                .sellerId(1L)
                .sellerName("Иван Иванов")
                .amount(new BigDecimal("1500.00"))
                .paymentType(PaymentType.CARD)
                .transactionDate(LocalDateTime.of(2026, 5, 20, 14, 30))
                .build();

        when(sellerService.getSellerTransactions(1L)).thenReturn(List.of(txDto));

        mockMvc.perform(get("/api/v1/sellers/1/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].amount").value(1500.00));
    }

    @Test
    void getSellerTransactions_shouldReturn404_whenSellerNotFound() throws Exception {
        when(sellerService.getSellerTransactions(99L)).thenThrow(NotFoundException.seller(99L));

        mockMvc.perform(get("/api/v1/sellers/99/transactions"))
                .andExpect(status().isNotFound());
    }
}
