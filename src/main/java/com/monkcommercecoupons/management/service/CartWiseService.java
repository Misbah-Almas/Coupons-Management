package com.monkcommercecoupons.management.service;

import com.monkcommercecoupons.management.model.dto.CartDTO;
import com.monkcommercecoupons.management.model.dto.UpdatedCartDTO;
import com.monkcommercecoupons.management.model.entity.Coupon;

public interface CartWiseService {
    /**
     * Calculates the discount amount applicable for a given coupon and cart.
     *
     * @param coupon The coupon to evaluate.
     * @param cart   The shopping cart details.
     * @return The calculated discount value.
     */
    double calculateDiscount(Coupon coupon, CartDTO cart);

    /**
     * Applies the given coupon discount to the cart and returns an updated cart.
     *
     * @param coupon The coupon to apply.
     * @param cart   The shopping cart details.
     * @return The updated cart with discounts and final price.
     */
    UpdatedCartDTO applyDiscount(Coupon coupon, CartDTO cart);
}
