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

import static org.assertj.core.api.Assertions.*;

class ProductWiseServiceTest {

    private ProductWiseService productWiseService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        productWiseService = new ProductWiseServiceImpl(objectMapper);
    }

    @Test
    void calculateDiscount_ProductInCart_ShouldCalculate() {
        Coupon coupon = Coupon.builder()
                .type(CouponType.PRODUCT_WISE)
                .details("{\"productId\":1,\"discount\":20.0,\"discountType\":\"PERCENTAGE\"}")
                .build();

        CartDTO cart = CartDTO.builder()
                .items(List.of(
                        CartItemDTO.builder()
                                .productId(1L)
                                .quantity(2)
                                .price(100.0)
                                .build()
                ))
                .build();

        double discount = productWiseService.calculateDiscount(coupon, cart);

        assertThat(discount).isEqualTo(40.0);
    }

    @Test
    void calculateDiscount_ProductNotInCart_ShouldThrowException() {
        Coupon coupon = Coupon.builder()
                .type(CouponType.PRODUCT_WISE)
                .details("{\"productId\":999,\"discount\":20.0,\"discountType\":\"PERCENTAGE\"}")
                .build();

        CartDTO cart = CartDTO.builder()
                .items(List.of(
                        CartItemDTO.builder()
                                .productId(1L)
                                .quantity(2)
                                .price(100.0)
                                .build()
                ))
                .build();

        assertThatThrownBy(() -> productWiseService.calculateDiscount(coupon, cart))
                .isInstanceOf(CouponNotApplicableException.class)
                .hasMessageContaining("not found in cart");
    }

    @Test
    void applyDiscount_ShouldReturnUpdatedCart() {
        Coupon coupon = Coupon.builder()
                .type(CouponType.PRODUCT_WISE)
                .details("{\"productId\":1,\"discount\":20.0,\"discountType\":\"PERCENTAGE\"}")
                .build();

        CartDTO cart = CartDTO.builder()
                .items(List.of(
                        CartItemDTO.builder()
                                .productId(1L)
                                .quantity(2)
                                .price(100.0)
                                .build(),
                        CartItemDTO.builder()
                                .productId(2L)
                                .quantity(1)
                                .price(50.0)
                                .build()
                ))
                .build();

        UpdatedCartDTO result = productWiseService.applyDiscount(coupon, cart);

        assertThat(result.getTotalPrice()).isEqualTo(250.0);
        assertThat(result.getTotalDiscount()).isEqualTo(40.0);
        assertThat(result.getFinalPrice()).isEqualTo(210.0);
    }
}
