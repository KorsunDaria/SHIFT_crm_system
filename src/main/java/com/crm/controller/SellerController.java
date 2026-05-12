package com.crm.controller;

import com.crm.dto.seller.SellerCreateDto;
import com.crm.dto.seller.SellerResponseDto;
import com.crm.dto.seller.SellerUpdateDto;
import com.crm.dto.transaction.TransactionResponseDto;
import com.crm.service.SellerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sellers")
@Tag(name = "Sellers", description = "Управление продавцами")
public class SellerController {

    private final SellerService sellerService;

    public SellerController(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    @GetMapping
    @Operation(summary = "Получить список всех продавцов")
    public ResponseEntity<List<SellerResponseDto>> getAllSellers() {
        return ResponseEntity.ok(sellerService.getAllSellers());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить продавца по ID")
    public ResponseEntity<SellerResponseDto> getSellerById(@PathVariable Long id) {
        return ResponseEntity.ok(sellerService.getSellerById(id));
    }

    @PostMapping
    @Operation(summary = "Создать нового продавца")
    public ResponseEntity<SellerResponseDto> createSeller(@Valid @RequestBody SellerCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sellerService.createSeller(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить данные продавца")
    public ResponseEntity<SellerResponseDto> updateSeller(
            @PathVariable Long id,
            @RequestBody SellerUpdateDto dto) {
        return ResponseEntity.ok(sellerService.updateSeller(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить продавца (soft delete)")
    public ResponseEntity<Void> deleteSeller(@PathVariable Long id) {
        sellerService.deleteSeller(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/transactions")
    @Operation(summary = "Получить все транзакции продавца")
    public ResponseEntity<List<TransactionResponseDto>> getSellerTransactions(@PathVariable Long id) {
        return ResponseEntity.ok(sellerService.getSellerTransactions(id));
    }
}