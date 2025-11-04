package com.monkcommercecoupons.management.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.monkcommercecoupons.management.exception.CouponNotFoundException;
import com.monkcommercecoupons.management.exception.DuplicateCouponCodeException;
import com.monkcommercecoupons.management.model.dto.CouponDTO;
import com.monkcommercecoupons.management.model.entity.Coupon;
import com.monkcommercecoupons.management.model.enums.CouponType;
import com.monkcommercecoupons.management.repository.CouponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CartWiseService cartWiseService;

    @Mock
    private ProductWiseService productWiseService;

    @Mock
    private BxGyService bxGyService;

    @InjectMocks
    private CouponServiceImpl couponService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        couponService = new CouponServiceImpl(
                couponRepository,
                cartWiseService,
                productWiseService,
                bxGyService,
                objectMapper
        );
    }

    @Test
    void createCoupon_ShouldCreateSuccessfully() {
        ObjectNode details = objectMapper.createObjectNode();
        details.put("threshold", 100.0);
        details.put("discount", 10.0);

        CouponDTO couponDTO = CouponDTO.builder()
                .code("SAVE10")
                .type(CouponType.CART_WISE)
                .description("10% off")
                .details(details)
                .isActive(true)
                .build();

        Coupon savedCoupon = Coupon.builder()
                .id(1L)
                .code("SAVE10")
                .type(CouponType.CART_WISE)
                .description("10% off")
                .details("{\"threshold\":100.0,\"discount\":10.0}")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(couponRepository.existsByCode("SAVE10")).thenReturn(false);
        when(couponRepository.save(any(Coupon.class))).thenReturn(savedCoupon);

        CouponDTO result = couponService.createCoupon(couponDTO);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCode()).isEqualTo("SAVE10");
        verify(couponRepository).save(any(Coupon.class));
    }

    @Test
    void createCoupon_WithDuplicateCode_ShouldThrowException() {
        ObjectNode details = objectMapper.createObjectNode();
        details.put("threshold", 100.0);

        CouponDTO couponDTO = CouponDTO.builder()
                .code("SAVE10")
                .type(CouponType.CART_WISE)
                .details(details)
                .build();

        when(couponRepository.existsByCode("SAVE10")).thenReturn(true);

        assertThatThrownBy(() -> couponService.createCoupon(couponDTO))
                .isInstanceOf(DuplicateCouponCodeException.class)
                .hasMessageContaining("SAVE10");
    }

    @Test
    void getCouponById_WhenExists_ShouldReturnCoupon() {
        Coupon coupon = Coupon.builder()
                .id(1L)
                .code("SAVE10")
                .type(CouponType.CART_WISE)
                .details("{\"threshold\":100.0}")
                .isActive(true)
                .build();

        when(couponRepository.findById(1L)).thenReturn(Optional.of(coupon));

        CouponDTO result = couponService.getCouponById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCode()).isEqualTo("SAVE10");
    }

    @Test
    void getCouponById_WhenNotExists_ShouldThrowException() {
        when(couponRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> couponService.getCouponById(999L))
                .isInstanceOf(CouponNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void getAllCoupons_ShouldReturnAllCoupons() {
        List<Coupon> coupons = List.of(
                Coupon.builder()
                        .id(1L)
                        .code("SAVE10")
                        .type(CouponType.CART_WISE)
                        .details("{\"threshold\":100.0}")
                        .build(),
                Coupon.builder()
                        .id(2L)
                        .code("PRODUCT20")
                        .type(CouponType.PRODUCT_WISE)
                        .details("{\"productId\":1}")
                        .build()
        );

        when(couponRepository.findAll()).thenReturn(coupons);

        List<CouponDTO> result = couponService.getAllCoupons();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCode()).isEqualTo("SAVE10");
        assertThat(result.get(1).getCode()).isEqualTo("PRODUCT20");
    }

    @Test
    void deleteCoupon_WhenExists_ShouldDeleteSuccessfully() {
        when(couponRepository.existsById(1L)).thenReturn(true);

        couponService.deleteCoupon(1L);

        verify(couponRepository).deleteById(1L);
    }

    @Test
    void deleteCoupon_WhenNotExists_ShouldThrowException() {
        when(couponRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> couponService.deleteCoupon(999L))
                .isInstanceOf(CouponNotFoundException.class);
    }
}
