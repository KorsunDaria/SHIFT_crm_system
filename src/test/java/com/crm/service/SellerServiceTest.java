package com.crm.service;

import com.crm.dto.seller.SellerCreateDto;
import com.crm.dto.seller.SellerResponseDto;
import com.crm.dto.seller.SellerUpdateDto;
import com.crm.entity.Seller;
import com.crm.exception.NotFoundException;
import com.crm.mapper.SellerMapper;
import com.crm.mapper.TransactionMapper;
import com.crm.repository.SellerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SellerServiceTest {

    @Mock
    private SellerRepository sellerRepository;
    @Mock
    private SellerMapper sellerMapper;
    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private SellerService sellerService;

    private Seller seller;
    private SellerResponseDto sellerResponseDto;

    @BeforeEach
    void setUp() {
        seller = new Seller();
        seller.setId(1L);
        seller.setName("Иван Иванов");
        seller.setContactInfo("ivan@example.com");
        seller.setRegistrationDate(LocalDateTime.now());

        sellerResponseDto = SellerResponseDto.builder()
                .id(1L)
                .name("Иван Иванов")
                .contactInfo("ivan@example.com")
                .registrationDate(seller.getRegistrationDate())
                .build();
    }

    @Test
    void getAllSellers_shouldReturnListOfSellers() {
        when(sellerRepository.findAllActive()).thenReturn(List.of(seller));
        when(sellerMapper.toDto(seller)).thenReturn(sellerResponseDto);

        List<SellerResponseDto> result = sellerService.getAllSellers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Иван Иванов");
        verify(sellerRepository).findAllActive();
    }

    @Test
    void getSellerById_shouldReturnSeller_whenExists() {
        when(sellerRepository.findActiveById(1L)).thenReturn(Optional.of(seller));
        when(sellerMapper.toDto(seller)).thenReturn(sellerResponseDto);

        SellerResponseDto result = sellerService.getSellerById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Иван Иванов");
    }

    @Test
    void getSellerById_shouldThrow_whenNotFound() {
        when(sellerRepository.findActiveById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sellerService.getSellerById(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void createSeller_shouldSaveAndReturnDto() {
        SellerCreateDto createDto = new SellerCreateDto();
        createDto.setName("Новый Продавец");
        createDto.setContactInfo("new@example.com");

        when(sellerMapper.toEntity(createDto)).thenReturn(seller);
        when(sellerRepository.save(seller)).thenReturn(seller);
        when(sellerMapper.toDto(seller)).thenReturn(sellerResponseDto);

        SellerResponseDto result = sellerService.createSeller(createDto);

        assertThat(result).isNotNull();
        verify(sellerRepository).save(seller);
    }

    @Test
    void updateSeller_shouldUpdateName() {
        SellerUpdateDto updateDto = new SellerUpdateDto();
        updateDto.setName("Обновлённое имя");

        when(sellerRepository.findActiveById(1L)).thenReturn(Optional.of(seller));
        when(sellerRepository.save(any(Seller.class))).thenReturn(seller);
        when(sellerMapper.toDto(seller)).thenReturn(sellerResponseDto);

        sellerService.updateSeller(1L, updateDto);

        assertThat(seller.getName()).isEqualTo("Обновлённое имя");
        verify(sellerRepository).save(seller);
    }

    @Test
    void deleteSeller_shouldSetDeletedAt() {
        when(sellerRepository.findActiveById(1L)).thenReturn(Optional.of(seller));
        when(sellerRepository.save(any(Seller.class))).thenReturn(seller);

        sellerService.deleteSeller(1L);

        assertThat(seller.getDeletedAt()).isNotNull();
        assertThat(seller.isDeleted()).isTrue();
        verify(sellerRepository).save(seller);
    }

    @Test
    void deleteSeller_shouldThrow_whenNotFound() {
        when(sellerRepository.findActiveById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sellerService.deleteSeller(99L))
                .isInstanceOf(NotFoundException.class);

        verify(sellerRepository, never()).save(any());
    }
}