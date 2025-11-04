package com.monkcommercecoupons.management.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monkcommercecoupons.management.exception.CouponNotApplicableException;
import com.monkcommercecoupons.management.model.dto.CartDTO;
import com.monkcommercecoupons.management.model.dto.CartItemDTO;
import com.monkcommercecoupons.management.model.entity.Coupon;
import com.monkcommercecoupons.management.model.enums.CouponType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class BxGyServiceTest {

    private BxGyService bxGyService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        bxGyService = new BxGyServiceImpl(objectMapper);
    }

    @Test
    void calculateDiscount_ValidBxGy_ShouldCalculate() {
        Coupon coupon = Coupon.builder()
                .type(CouponType.BXGY)
                .details("{\"buyProducts\":[{\"productId\":1,\"quantity\":2}]," +
                        "\"getProducts\":[{\"productId\":3,\"quantity\":1}]," +
                        "\"repetitionLimit\":1}")
                .build();

        CartDTO cart = CartDTO.builder()
                .items(List.of(
                        CartItemDTO.builder()
                                .productId(1L)
                                .quantity(2)
                                .price(100.0)
                                .build(),
                        CartItemDTO.builder()
                                .productId(3L)
                                .quantity(1)
                                .price(50.0)
                                .build()
                ))
                .build();

        double discount = bxGyService.calculateDiscount(coupon, cart);

        assertThat(discount).isEqualTo(50.0);
    }

    @Test
    void calculateDiscount_InsufficientBuyProducts_ShouldThrowException() {
        Coupon coupon = Coupon.builder()
                .type(CouponType.BXGY)
                .details("{\"buyProducts\":[{\"productId\":1,\"quantity\":5}]," +
                        "\"getProducts\":[{\"productId\":3,\"quantity\":1}]," +
                        "\"repetitionLimit\":1}")
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

        assertThatThrownBy(() -> bxGyService.calculateDiscount(coupon, cart))
                .isInstanceOf(CouponNotApplicableException.class)
                .hasMessageContaining("Insufficient buy products");
    }
}
