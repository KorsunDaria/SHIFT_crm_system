package com.crm.dto.seller;

import java.time.LocalDateTime;

public class SellerResponseDto {

    private Long id;
    private String name;
    private String contactInfo;
    private LocalDateTime registrationDate;

    private SellerResponseDto() {}

    public static Builder builder() { return new Builder(); }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getContactInfo() { return contactInfo; }
    public LocalDateTime getRegistrationDate() { return registrationDate; }

    public static class Builder {
        private final SellerResponseDto dto = new SellerResponseDto();

        public Builder id(Long id) { dto.id = id; return this; }
        public Builder name(String name) { dto.name = name; return this; }
        public Builder contactInfo(String contactInfo) { dto.contactInfo = contactInfo; return this; }
        public Builder registrationDate(LocalDateTime registrationDate) { dto.registrationDate = registrationDate; return this; }

        public SellerResponseDto build() { return dto; }
    }
}