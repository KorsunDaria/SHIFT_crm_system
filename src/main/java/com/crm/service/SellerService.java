package com.crm.service;

import com.crm.dto.seller.SellerCreateDto;
import com.crm.dto.seller.SellerResponseDto;
import com.crm.dto.seller.SellerUpdateDto;
import com.crm.dto.transaction.TransactionResponseDto;
import com.crm.entity.Seller;
import com.crm.exception.NotFoundException;
import com.crm.mapper.SellerMapper;
import com.crm.mapper.TransactionMapper;
import com.crm.repository.SellerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SellerService {

    private final SellerRepository sellerRepository;
    private final SellerMapper sellerMapper;
    private final TransactionMapper transactionMapper;

    public SellerService(SellerRepository sellerRepository,
                         SellerMapper sellerMapper,
                         TransactionMapper transactionMapper) {
        this.sellerRepository = sellerRepository;
        this.sellerMapper = sellerMapper;
        this.transactionMapper = transactionMapper;
    }

    @Transactional(readOnly = true)
    public List<SellerResponseDto> getAllSellers() {
        return sellerRepository.findAllActive()
                .stream()
                .map(sellerMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public SellerResponseDto getSellerById(Long id) {
        return sellerMapper.toDto(findActiveOrThrow(id));
    }

    @Transactional
    public SellerResponseDto createSeller(SellerCreateDto dto) {
        Seller seller = sellerMapper.toEntity(dto);
        return sellerMapper.toDto(sellerRepository.save(seller));
    }

    @Transactional
    public SellerResponseDto updateSeller(Long id, SellerUpdateDto dto) {
        Seller seller = findActiveOrThrow(id);

        if (dto.getName() != null && !dto.getName().isBlank()) {
            seller.setName(dto.getName());
        }
        if (dto.getContactInfo() != null) {
            seller.setContactInfo(dto.getContactInfo());
        }

        return sellerMapper.toDto(sellerRepository.save(seller));
    }

    @Transactional
    public void deleteSeller(Long id) {
        Seller seller = findActiveOrThrow(id);
        seller.setDeletedAt(LocalDateTime.now());
        sellerRepository.save(seller);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponseDto> getSellerTransactions(Long sellerId) {
        Seller seller = findActiveOrThrow(sellerId);
        return seller.getTransactions()
                .stream()
                .map(transactionMapper::toDto)
                .toList();
    }

    private Seller findActiveOrThrow(Long id) {
        return sellerRepository.findActiveById(id)
                .orElseThrow(() -> NotFoundException.seller(id));
    }
}