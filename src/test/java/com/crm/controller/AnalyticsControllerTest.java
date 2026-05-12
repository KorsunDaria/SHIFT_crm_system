package com.crm.controller;

import com.crm.dto.analytics.BestPeriodResponseDto;
import com.crm.dto.analytics.BestSellerResponseDto;
import com.crm.dto.analytics.LowPerformerSellerDto;
import com.crm.exception.GlobalExceptionHandler;
import com.crm.exception.NotFoundException;
import com.crm.service.AnalyticsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsControllerTest {

    @Mock
    private AnalyticsService analyticsService;

    @InjectMocks
    private AnalyticsController analyticsController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(analyticsController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Test
    void getBestSeller_shouldReturn200_withDefaultPeriod() throws Exception {
        BestSellerResponseDto response = BestSellerResponseDto.builder()
                .sellerId(1L)
                .sellerName("Лучший продавец")
                .totalAmount(new BigDecimal("99000.00"))
                .period("MONTH")
                .build();

        when(analyticsService.getBestSeller("MONTH")).thenReturn(response);

        mockMvc.perform(get("/api/v1/analytics/best-seller"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sellerId").value(1))
                .andExpect(jsonPath("$.sellerName").value("Лучший продавец"))
                .andExpect(jsonPath("$.period").value("MONTH"));
    }

    @Test
    void getBestSeller_shouldReturn200_forDayPeriod() throws Exception {
        BestSellerResponseDto response = BestSellerResponseDto.builder()
                .sellerId(2L)
                .sellerName("Дневной лидер")
                .totalAmount(new BigDecimal("5000.00"))
                .period("DAY")
                .build();

        when(analyticsService.getBestSeller("DAY")).thenReturn(response);

        mockMvc.perform(get("/api/v1/analytics/best-seller").param("period", "DAY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.period").value("DAY"))
                .andExpect(jsonPath("$.totalAmount").value(5000.00));
    }

    @Test
    void getBestSeller_shouldReturn404_whenNoTransactions() throws Exception {
        when(analyticsService.getBestSeller("DAY"))
                .thenThrow(new NotFoundException("За период 'DAY' транзакций не найдено"));

        mockMvc.perform(get("/api/v1/analytics/best-seller").param("period", "DAY"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBestSeller_shouldReturn400_whenInvalidPeriod() throws Exception {
        when(analyticsService.getBestSeller("WEEK"))
                .thenThrow(new IllegalArgumentException("Неверный период"));

        mockMvc.perform(get("/api/v1/analytics/best-seller").param("period", "WEEK"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getLowPerformers_shouldReturn200WithList() throws Exception {
        LowPerformerSellerDto dto = new LowPerformerSellerDto();
        dto.setSellerId(3L);
        dto.setSellerName("Слабый продавец");
        dto.setTotalAmount(new BigDecimal("300.00"));

        when(analyticsService.getSellersWithTotalBelow(any(), any(), any()))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/analytics/low-performers")
                        .param("from", "2026-01-01T00:00:00")
                        .param("to", "2026-12-31T23:59:59")
                        .param("amount", "1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sellerId").value(3))
                .andExpect(jsonPath("$[0].sellerName").value("Слабый продавец"))
                .andExpect(jsonPath("$[0].totalAmount").value(300.00));
    }

    @Test
    void getLowPerformers_shouldReturnEmptyList_whenAllAboveThreshold() throws Exception {
        when(analyticsService.getSellersWithTotalBelow(any(), any(), any()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/analytics/low-performers")
                        .param("from", "2026-01-01T00:00:00")
                        .param("to", "2026-12-31T23:59:59")
                        .param("amount", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getLowPerformers_shouldReturn400_whenFromAfterTo() throws Exception {
        when(analyticsService.getSellersWithTotalBelow(any(), any(), any()))
                .thenThrow(new IllegalArgumentException("Дата начала не может быть позже даты окончания"));

        mockMvc.perform(get("/api/v1/analytics/low-performers")
                        .param("from", "2026-12-31T00:00:00")
                        .param("to", "2026-01-01T00:00:00")
                        .param("amount", "1000"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Дата начала")));
    }


    @Test
    void getLowPerformers_shouldReturn400_whenParamsMissing() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/low-performers")
                        .param("from", "2026-01-01T00:00:00")) // Передаем только один вместо трех
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBestPeriod_shouldReturn200() throws Exception {
        BestPeriodResponseDto response = BestPeriodResponseDto.builder()
                .sellerId(1L)
                .sellerName("Активный продавец")
                .periodStart(LocalDateTime.of(2026, 3, 1, 9, 0))
                .periodEnd(LocalDateTime.of(2026, 3, 3, 18, 0))
                .transactionCount(12)
                .totalAmount(new BigDecimal("24000.00"))
                .build();

        when(analyticsService.getBestPeriodForSeller(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/analytics/best-period/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sellerId").value(1))
                .andExpect(jsonPath("$.transactionCount").value(12))
                .andExpect(jsonPath("$.totalAmount").value(24000.00));
    }

    @Test
    void getBestPeriod_shouldReturn404_whenSellerNotFound() throws Exception {
        when(analyticsService.getBestPeriodForSeller(99L))
                .thenThrow(NotFoundException.seller(99L));

        mockMvc.perform(get("/api/v1/analytics/best-period/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getBestPeriod_shouldReturn404_whenNoTransactions() throws Exception {
        when(analyticsService.getBestPeriodForSeller(1L))
                .thenThrow(new NotFoundException("У продавца с id=1 нет транзакций"));

        mockMvc.perform(get("/api/v1/analytics/best-period/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("нет транзакций")));
    }
}
