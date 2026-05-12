package com.crm.service;

import com.crm.dto.analytics.BestPeriodResponseDto;
import com.crm.dto.analytics.BestSellerResponseDto;
import com.crm.dto.analytics.LowPerformerSellerDto;
import com.crm.entity.Seller;
import com.crm.entity.Transaction;
import com.crm.enums.PaymentType;
import com.crm.exception.NotFoundException;
import com.crm.repository.SellerRepository;
import com.crm.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private SellerRepository sellerRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    private Seller seller;

    @BeforeEach
    void setUp() {
        seller = new Seller();
        seller.setId(1L);
        seller.setName("Тест Продавец");
        seller.setContactInfo("test@mail.ru");
    }


    @Test
    void getBestSeller_shouldReturnTopSeller_forDayPeriod() {
        Object[] row = new Object[]{1L, new BigDecimal("3000.00")};
        List<Object[]> mockResult = new java.util.ArrayList<>();
        mockResult.add(row);
        when(transactionRepository.findSellerIdAndTotalAmountInPeriod(any(), any()))
                .thenReturn(mockResult);
        when(sellerRepository.findActiveById(1L)).thenReturn(Optional.of(seller));

        BestSellerResponseDto result = analyticsService.getBestSeller("DAY");

        assertThat(result.getSellerId()).isEqualTo(1L);
        assertThat(result.getSellerName()).isEqualTo("Тест Продавец");
        assertThat(result.getTotalAmount()).isEqualByComparingTo("3000.00");
        assertThat(result.getPeriod()).isEqualTo("DAY");
    }

    @Test
    void getBestSeller_shouldReturnTopSeller_forMonthPeriod() {
        Object[] row = new Object[]{1L, new BigDecimal("15000.00")};
        List<Object[]> mockResult = new java.util.ArrayList<>();
        mockResult.add(row);
        when(transactionRepository.findSellerIdAndTotalAmountInPeriod(any(), any()))
                .thenReturn(mockResult);
        when(sellerRepository.findActiveById(1L)).thenReturn(Optional.of(seller));

        BestSellerResponseDto result = analyticsService.getBestSeller("MONTH");

        assertThat(result.getPeriod()).isEqualTo("MONTH");
        assertThat(result.getTotalAmount()).isEqualByComparingTo("15000.00");
    }

    @Test
    void getBestSeller_shouldReturnTopSeller_forQuarterPeriod() {
        Object[] row = new Object[]{1L, new BigDecimal("50000.00")};
        List<Object[]> mockResult = new java.util.ArrayList<>();
        mockResult.add(row);
        when(transactionRepository.findSellerIdAndTotalAmountInPeriod(any(), any()))
                .thenReturn(mockResult);
        when(sellerRepository.findActiveById(1L)).thenReturn(Optional.of(seller));

        BestSellerResponseDto result = analyticsService.getBestSeller("QUARTER");

        assertThat(result.getPeriod()).isEqualTo("QUARTER");
    }

    @Test
    void getBestSeller_shouldReturnTopSeller_forYearPeriod() {
        Object[] row = new Object[]{1L, new BigDecimal("200000.00")};
        List<Object[]> mockResult = new java.util.ArrayList<>();
        mockResult.add(row);
        when(transactionRepository.findSellerIdAndTotalAmountInPeriod(any(), any()))
                .thenReturn(mockResult);
        when(sellerRepository.findActiveById(1L)).thenReturn(Optional.of(seller));

        BestSellerResponseDto result = analyticsService.getBestSeller("YEAR");

        assertThat(result.getPeriod()).isEqualTo("YEAR");
    }

    @Test
    void getBestSeller_shouldThrow_whenNoTransactions() {
        when(transactionRepository.findSellerIdAndTotalAmountInPeriod(any(), any()))
                .thenReturn(List.of());

        assertThatThrownBy(() -> analyticsService.getBestSeller("MONTH"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("MONTH");
    }

    @Test
    void getBestSeller_shouldThrow_whenInvalidPeriod() {
        assertThatThrownBy(() -> analyticsService.getBestSeller("WEEK"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("DAY, MONTH, QUARTER, YEAR");
    }


    @Test
    void getSellersWithTotalBelow_shouldReturnList() {
        LocalDateTime from = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 12, 31, 23, 59);

        Object[] row = new Object[]{1L, new BigDecimal("800.00")};
        List<Object[]> mockResult = new java.util.ArrayList<>();
        mockResult.add(row);

        when(transactionRepository.findSellersBelowThreshold(from, to, new BigDecimal("1000")))
                .thenReturn(mockResult);
        when(sellerRepository.findAllById(List.of(1L))).thenReturn(List.of(seller));

        List<LowPerformerSellerDto> result = analyticsService.getSellersWithTotalBelow(
                from, to, new BigDecimal("1000"));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSellerId()).isEqualTo(1L);
        assertThat(result.get(0).getTotalAmount()).isEqualByComparingTo("800.00");
    }

    @Test
    void getSellersWithTotalBelow_shouldReturnEmpty_whenNoneBelow() {
        LocalDateTime from = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 12, 31, 23, 59);

        when(transactionRepository.findSellersBelowThreshold(from, to, new BigDecimal("100")))
                .thenReturn(List.of());
        when(sellerRepository.findAllById(List.of())).thenReturn(List.of());

        List<LowPerformerSellerDto> result = analyticsService.getSellersWithTotalBelow(
                from, to, new BigDecimal("100"));

        assertThat(result).isEmpty();
    }

    @Test
    void getSellersWithTotalBelow_shouldThrow_whenFromAfterTo() {
        LocalDateTime from = LocalDateTime.of(2026, 12, 31, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 1, 1, 0, 0);

        assertThatThrownBy(() -> analyticsService.getSellersWithTotalBelow(from, to, BigDecimal.TEN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Дата начала");
    }


    @Test
    void getBestPeriod_shouldReturnSingleTransaction_whenOnlyOne() {
        Transaction t = makeTransaction(1L, "500.00", LocalDateTime.of(2026, 6, 1, 10, 0));

        when(sellerRepository.findActiveById(1L)).thenReturn(Optional.of(seller));
        when(transactionRepository.findBySellerIdSortedByDate(1L)).thenReturn(List.of(t));

        BestPeriodResponseDto result = analyticsService.getBestPeriodForSeller(1L);

        assertThat(result.getTransactionCount()).isEqualTo(1);
        assertThat(result.getTotalAmount()).isEqualByComparingTo("500.00");
    }

    @Test
    void getBestPeriod_shouldFindDenseCluster() {
        LocalDateTime base = LocalDateTime.of(2026, 6, 1, 10, 0);
        Transaction t1 = makeTransaction(1L, "100.00", base);
        Transaction t2 = makeTransaction(2L, "200.00", base.plusMinutes(10));
        Transaction t3 = makeTransaction(3L, "300.00", base.plusMinutes(20)); // Конец периода будет здесь!
        Transaction t4 = makeTransaction(4L, "50.00", base.plusDays(30));

        when(sellerRepository.findActiveById(1L)).thenReturn(Optional.of(seller));
        when(transactionRepository.findBySellerIdSortedByDate(1L))
                .thenReturn(List.of(t1, t2, t3, t4));


        BestPeriodResponseDto result = analyticsService.getBestPeriodForSeller(1L);


        assertThat(result.getTransactionCount()).isEqualTo(3);
        assertThat(result.getPeriodStart()).isEqualTo(base);


        assertThat(result.getPeriodEnd()).isEqualTo(base.plusMinutes(20));
    }

    @Test
    void getBestPeriod_shouldThrow_whenSellerNotFound() {
        when(sellerRepository.findActiveById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> analyticsService.getBestPeriodForSeller(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getBestPeriod_shouldThrow_whenNoTransactions() {
        when(sellerRepository.findActiveById(1L)).thenReturn(Optional.of(seller));
        when(transactionRepository.findBySellerIdSortedByDate(1L)).thenReturn(List.of());

        assertThatThrownBy(() -> analyticsService.getBestPeriodForSeller(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("нет транзакций");
    }

    @Test
    void getBestPeriod_shouldCalculateTotalAmountCorrectly() {
        LocalDateTime base = LocalDateTime.of(2026, 3, 15, 9, 0);
        Transaction t1 = makeTransaction(1L, "1000.00", base);
        Transaction t2 = makeTransaction(2L, "2000.00", base.plusHours(1));

        when(sellerRepository.findActiveById(1L)).thenReturn(Optional.of(seller));
        when(transactionRepository.findBySellerIdSortedByDate(1L))
                .thenReturn(List.of(t1, t2));

        BestPeriodResponseDto result = analyticsService.getBestPeriodForSeller(1L);

        assertThat(result.getTotalAmount()).isEqualByComparingTo("3000.00");
        assertThat(result.getSellerName()).isEqualTo("Тест Продавец");
        assertThat(result.getSellerId()).isEqualTo(1L);
    }



    private Transaction makeTransaction(Long id, String amount, LocalDateTime date) {
        Transaction t = new Transaction();
        t.setId(id);
        t.setSeller(seller);
        t.setAmount(new BigDecimal(amount));
        t.setPaymentType(PaymentType.CASH);
        t.setTransactionDate(date);
        return t;
    }
}
