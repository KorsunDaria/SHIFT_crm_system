package com.crm.service;

import com.crm.dto.analytics.BestPeriodResponseDto;
import com.crm.dto.analytics.BestSellerResponseDto;
import com.crm.dto.analytics.LowPerformerSellerDto;
import com.crm.entity.Seller;
import com.crm.entity.Transaction;
import com.crm.exception.NotFoundException;
import com.crm.repository.SellerRepository;
import com.crm.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.IsoFields;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final TransactionRepository transactionRepository;
    private final SellerRepository sellerRepository;

    public AnalyticsService(TransactionRepository transactionRepository,
                            SellerRepository sellerRepository) {
        this.transactionRepository = transactionRepository;
        this.sellerRepository = sellerRepository;
    }

    @Transactional(readOnly = true)
    public BestSellerResponseDto getBestSeller(String period) {
        LocalDateTime[] range = resolvePeriodRange(period);

        List<Object[]> results = transactionRepository.findSellerIdAndTotalAmountInPeriod(range[0], range[1]);

        if (results.isEmpty()) {
            throw new NotFoundException("За период '" + period + "' транзакций не найдено");
        }

        Object[] top = results.get(0);
        Long sellerId = (Long) top[0];
        BigDecimal totalAmount = (BigDecimal) top[1];

        Seller seller = sellerRepository.findActiveById(sellerId)
                .orElseThrow(() -> NotFoundException.seller(sellerId));

        return BestSellerResponseDto.builder()
                .sellerId(seller.getId())
                .sellerName(seller.getName())
                .totalAmount(totalAmount)
                .period(period.toUpperCase())
                .build();
    }

    @Transactional(readOnly = true)
    public List<LowPerformerSellerDto> getSellersWithTotalBelow(
            LocalDateTime from, LocalDateTime to, BigDecimal threshold) {

        if (from.isAfter(to)) {
            throw new IllegalArgumentException("Дата начала не может быть позже даты окончания");
        }

        List<Object[]> results = transactionRepository.findSellersBelowThreshold(from, to, threshold);

        List<Long> sellerIds = results.stream()
                .map(row -> (Long) row[0])
                .toList();

        Map<Long, Seller> sellerMap = sellerRepository.findAllById(sellerIds)
                .stream()
                .collect(Collectors.toMap(Seller::getId, s -> s));

        return results.stream()
                .map(row -> {
                    Long sellerId = (Long) row[0];
                    BigDecimal total = (BigDecimal) row[1];
                    Seller seller = sellerMap.get(sellerId);

                    LowPerformerSellerDto dto = new LowPerformerSellerDto();
                    dto.setSellerId(sellerId);
                    dto.setSellerName(seller != null ? seller.getName() : "Неизвестно");
                    dto.setTotalAmount(total);
                    return dto;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public BestPeriodResponseDto getBestPeriodForSeller(Long sellerId) {
        Seller seller = sellerRepository.findActiveById(sellerId)
                .orElseThrow(() -> NotFoundException.seller(sellerId));

        List<Transaction> transactions = transactionRepository.findBySellerIdSortedByDate(sellerId);

        if (transactions.isEmpty()) {
            throw new NotFoundException("У продавца с id=" + sellerId + " нет транзакций");
        }

        if (transactions.size() == 1) {
            Transaction t = transactions.get(0);
            return BestPeriodResponseDto.builder()
                    .sellerId(sellerId)
                    .sellerName(seller.getName())
                    .periodStart(t.getTransactionDate())
                    .periodEnd(t.getTransactionDate())
                    .transactionCount(1)
                    .totalAmount(t.getAmount())
                    .build();
        }

        int bestLeft = 0;
        int bestRight = 0;
        double bestDensity = 0;

        for (int left = 0; left < transactions.size(); left++) {
            for (int right = left; right < transactions.size(); right++) {
                LocalDateTime start = transactions.get(left).getTransactionDate();
                LocalDateTime end = transactions.get(right).getTransactionDate();

                int count = right - left + 1;
                long durationHours = java.time.Duration.between(start, end).toHours();
                double density = count / (double) Math.max(durationHours, 1);

                if (density > bestDensity) {
                    bestDensity = density;
                    bestLeft = left;
                    bestRight = right;
                }
            }
        }

        List<Transaction> bestWindow = transactions.subList(bestLeft, bestRight + 1);
        BigDecimal totalAmount = bestWindow.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return BestPeriodResponseDto.builder()
                .sellerId(sellerId)
                .sellerName(seller.getName())
                .periodStart(transactions.get(bestLeft).getTransactionDate())
                .periodEnd(transactions.get(bestRight).getTransactionDate())
                .transactionCount(bestWindow.size())
                .totalAmount(totalAmount)
                .build();
    }

    private LocalDateTime[] resolvePeriodRange(String period) {
        LocalDateTime now = LocalDateTime.now();
        return switch (period.toUpperCase()) {
            case "DAY" -> new LocalDateTime[]{
                    now.toLocalDate().atStartOfDay(),
                    now.toLocalDate().atTime(23, 59, 59)
            };
            case "MONTH" -> new LocalDateTime[]{
                    now.withDayOfMonth(1).toLocalDate().atStartOfDay(),
                    now.withDayOfMonth(now.toLocalDate().lengthOfMonth()).toLocalDate().atTime(23, 59, 59)
            };
            case "QUARTER" -> {
                int quarter = now.get(IsoFields.QUARTER_OF_YEAR);
                int startMonth = (quarter - 1) * 3 + 1;
                LocalDateTime start = LocalDateTime.of(now.getYear(), startMonth, 1, 0, 0);
                LocalDateTime end = start.plusMonths(3).minusSeconds(1);
                yield new LocalDateTime[]{start, end};
            }
            case "YEAR" -> new LocalDateTime[]{
                    LocalDateTime.of(now.getYear(), 1, 1, 0, 0),
                    LocalDateTime.of(now.getYear(), 12, 31, 23, 59, 59)
            };
            default -> throw new IllegalArgumentException(
                    "Неверный период. Допустимые значения: DAY, MONTH, QUARTER, YEAR"
            );
        };
    }
}