package com.crm.repository;

import com.crm.entity.Seller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SellerRepository extends JpaRepository<Seller, Long> {

    @Query("SELECT s FROM Seller s WHERE s.deletedAt IS NULL")
    List<Seller> findAllActive();

    @Query("SELECT s FROM Seller s WHERE s.id = :id AND s.deletedAt IS NULL")
    Optional<Seller> findActiveById(Long id);
}
