package com.crm.controller;

import com.crm.dto.analytics.BestPeriodResponseDto;
import com.crm.dto.analytics.BestSellerResponseDto;
import com.crm.dto.analytics.LowPerformerSellerDto;
import com.crm.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
@Tag(name = "Analytics", description = "Аналитика по продавцам и транзакциям")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/best-seller")
    @Operation(summary = "Самый продуктивный продавец за период",
            description = "period: DAY, MONTH, QUARTER, YEAR")
    public ResponseEntity<BestSellerResponseDto> getBestSeller(
            @Parameter(description = "Период: DAY, MONTH, QUARTER, YEAR")
            @RequestParam(defaultValue = "MONTH") String period) {
        return ResponseEntity.ok(analyticsService.getBestSeller(period));
    }

    @GetMapping("/low-performers")
    @Operation(summary = "Продавцы с суммой транзакций ниже порога")
    public ResponseEntity<List<LowPerformerSellerDto>> getLowPerformers(
            @Parameter(description = "Начало периода (yyyy-MM-dd'T'HH:mm:ss)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @Parameter(description = "Конец периода (yyyy-MM-dd'T'HH:mm:ss)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @Parameter(description = "Пороговая сумма")
            @RequestParam BigDecimal amount) {
        return ResponseEntity.ok(analyticsService.getSellersWithTotalBelow(from, to, amount));
    }

    @GetMapping("/best-period/{sellerId}")
    @Operation(summary = "Лучший период активности для конкретного продавца")
    public ResponseEntity<BestPeriodResponseDto> getBestPeriod(@PathVariable Long sellerId) {
        return ResponseEntity.ok(analyticsService.getBestPeriodForSeller(sellerId));
    }
}