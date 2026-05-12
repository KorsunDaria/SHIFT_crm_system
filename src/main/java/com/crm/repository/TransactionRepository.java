package com.crm.repository;

import com.crm.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findBySellerIdOrderByTransactionDateDesc(Long sellerId);

    @Query("""
        SELECT t.seller.id, SUM(t.amount)
        FROM Transaction t
        WHERE t.transactionDate >= :from AND t.transactionDate <= :to
        GROUP BY t.seller.id
        ORDER BY SUM(t.amount) DESC
        """)
    List<Object[]> findSellerIdAndTotalAmountInPeriod(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query("""
        SELECT t.seller.id, SUM(t.amount) as total
        FROM Transaction t
        WHERE t.transactionDate >= :from AND t.transactionDate <= :to
        GROUP BY t.seller.id
        HAVING SUM(t.amount) < :threshold
        """)
    List<Object[]> findSellersBelowThreshold(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("threshold") BigDecimal threshold
    );

    @Query("""
        SELECT t FROM Transaction t
        WHERE t.seller.id = :sellerId
        ORDER BY t.transactionDate ASC
        """)
    List<Transaction> findBySellerIdSortedByDate(@Param("sellerId") Long sellerId);
}
