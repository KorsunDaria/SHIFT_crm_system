package com.crm.mapper;

import com.crm.dto.seller.SellerCreateDto;
import com.crm.dto.seller.SellerResponseDto;
import com.crm.entity.Seller;
import org.springframework.stereotype.Component;

@Component
public class SellerMapper {

    public Seller toEntity(SellerCreateDto dto) {
        Seller seller = new Seller();
        seller.setName(dto.getName());
        seller.setContactInfo(dto.getContactInfo());
        return seller;
    }

    public SellerResponseDto toDto(Seller seller) {
        return SellerResponseDto.builder()
                .id(seller.getId())
                .name(seller.getName())
                .contactInfo(seller.getContactInfo())
                .registrationDate(seller.getRegistrationDate())
                .build();
    }
}