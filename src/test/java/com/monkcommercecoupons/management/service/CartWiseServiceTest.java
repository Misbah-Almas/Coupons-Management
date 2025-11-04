package com.monkcommercecoupons.management.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monkcommercecoupons.management.exception.CouponNotApplicableException;
import com.monkcommercecoupons.management.model.dto.CartDTO;
import com.monkcommercecoupons.management.model.dto.CartItemDTO;
import com.monkcommercecoupons.management.model.dto.UpdatedCartDTO;
import com.monkcommercecoupons.management.model.entity.Coupon;
import com.monkcommercecoupons.management.model.enums.CouponType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CartWiseServiceTest {

    private CartWiseService cartWiseService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        cartWiseService = new CartWiseServiceImpl(objectMapper);
    }

    @Test
    void calculateDiscount_PercentageDiscount_ShouldCalculateCorrectly() {
        Coupon coupon = Coupon.builder()
                .type(CouponType.CART_WISE)
                .details("{\"threshold\":100.0,\"discount\":10.0,\"discountType\":\"PERCENTAGE\"}")
                .build();

        CartDTO cart = CartDTO.builder()
                .items(List.of(
                        CartItemDTO.builder()
                                .productId(1L)
                                .quantity(2)
                                .price(60.0)
                                .build()
                ))
                .build();

        double discount = cartWiseService.calculateDiscount(coupon, cart);

        assertThat(discount).isEqualTo(12.0);
    }

    @Test
    void calculateDiscount_FixedDiscount_ShouldCalculateCorrectly() {
        Coupon coupon = Coupon.builder()
                .type(CouponType.CART_WISE)
                .details("{\"threshold\":100.0,\"discount\":50.0,\"discountType\":\"FIXED\"}")
                .build();

        CartDTO cart = CartDTO.builder()
                .items(List.of(
                        CartItemDTO.builder()
                                .productId(1L)
                                .quantity(2)
                                .price(60.0)
                                .build()
                ))
                .build();

        double discount = cartWiseService.calculateDiscount(coupon, cart);

        assertThat(discount).isEqualTo(50.0);
    }

    @Test
    void calculateDiscount_WithMaxCap_ShouldApplyCap() {
        Coupon coupon = Coupon.builder()
                .type(CouponType.CART_WISE)
                .details("{\"threshold\":100.0,\"discount\":20.0,\"discountType\":\"PERCENTAGE\",\"maxDiscount\":15.0}")
                .build();

        CartDTO cart = CartDTO.builder()
                .items(List.of(
                        CartItemDTO.builder()
                                .productId(1L)
                                .quantity(2)
                                .price(60.0)
                                .build()
                ))
                .build();

        double discount = cartWiseService.calculateDiscount(coupon, cart);

        assertThat(discount).isEqualTo(15.0);
    }

    @Test
    void calculateDiscount_BelowThreshold_ShouldThrowException() {
        Coupon coupon = Coupon.builder()
                .type(CouponType.CART_WISE)
                .details("{\"threshold\":200.0,\"discount\":10.0}")
                .build();

        CartDTO cart = CartDTO.builder()
                .items(List.of(
                        CartItemDTO.builder()
                                .productId(1L)
                                .quantity(1)
                                .price(50.0)
                                .build()
                ))
                .build();

        assertThatThrownBy(() -> cartWiseService.calculateDiscount(coupon, cart))
                .isInstanceOf(CouponNotApplicableException.class)
                .hasMessageContaining("below threshold");
    }

    @Test
    void applyDiscount_ShouldReturnUpdatedCart() {
        Coupon coupon = Coupon.builder()
                .type(CouponType.CART_WISE)
                .details("{\"threshold\":100.0,\"discount\":10.0,\"discountType\":\"PERCENTAGE\"}")
                .build();

        CartDTO cart = CartDTO.builder()
                .items(List.of(
                        CartItemDTO.builder()
                                .productId(1L)
                                .quantity(2)
                                .price(50.0)
                                .build(),
                        CartItemDTO.builder()
                                .productId(2L)
                                .quantity(1)
                                .price(50.0)
                                .build()
                ))
                .build();

        UpdatedCartDTO result = cartWiseService.applyDiscount(coupon, cart);

        assertThat(result.getTotalPrice()).isEqualTo(150.0);
        assertThat(result.getTotalDiscount()).isEqualTo(15.0);
        assertThat(result.getFinalPrice()).isEqualTo(135.0);
        assertThat(result.getItems()).hasSize(2);
    }
}
