package com.monkcommercecoupons.management.service;

import com.monkcommercecoupons.management.model.dto.CartDTO;
import com.monkcommercecoupons.management.model.dto.UpdatedCartDTO;
import com.monkcommercecoupons.management.model.entity.Coupon;

public interface BxGyService {
    /**
     * Calculates the discount amount for a given coupon and cart.
     *
     * @param coupon The coupon to be applied.
     * @param cart   The user's shopping cart.
     * @return The total discount amount.
     */
    double calculateDiscount(Coupon coupon, CartDTO cart);

    /**
     * Applies the discount to the given cart based on the coupon.
     *
     * @param coupon The coupon to be applied.
     * @param cart   The user's shopping cart.
     * @return Updated cart details after applying the discount.
     */
    UpdatedCartDTO applyDiscount(Coupon coupon, CartDTO cart);
}
