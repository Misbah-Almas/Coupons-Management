package com.monkcommercecoupons.management.service;

import com.monkcommercecoupons.management.model.dto.CartDTO;
import com.monkcommercecoupons.management.model.dto.UpdatedCartDTO;
import com.monkcommercecoupons.management.model.entity.Coupon;

public interface ProductWiseService {
    /**
     * Calculates the discount for a given product-wise coupon
     * based on the items present in the cart.
     *
     * @param coupon the product-wise coupon containing discount details
     * @param cart the shopping cart for which the discount needs to be calculated
     * @return the calculated discount amount
     */
    double calculateDiscount(Coupon coupon, CartDTO cart);

    /**
     * Applies the product-wise discount to the given cart and
     * returns an updated cart object containing price adjustments.
     *
     * @param coupon the coupon to be applied
     * @param cart the cart on which the discount should be applied
     * @return the updated cart with applied discounts and recalculated prices
     */
    UpdatedCartDTO applyDiscount(Coupon coupon, CartDTO cart);
}
