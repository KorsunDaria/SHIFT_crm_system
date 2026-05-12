package com.crm.dto.seller;

import jakarta.validation.constraints.NotBlank;

public class SellerCreateDto {

    @NotBlank(message = "Имя продавца не может быть пустым")
    private String name;

    private String contactInfo;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }
}