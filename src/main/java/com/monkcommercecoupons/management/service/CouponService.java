package com.monkcommercecoupons.management.service;

import com.monkcommercecoupons.management.model.dto.ApplicableCouponsResponse;
import com.monkcommercecoupons.management.model.dto.ApplyCouponResponse;
import com.monkcommercecoupons.management.model.dto.CartRequest;
import com.monkcommercecoupons.management.model.dto.CouponDTO;

import java.util.List;

public interface CouponService {

    CouponDTO createCoupon(CouponDTO couponDTO);

    List<CouponDTO> getAllCoupons();

    CouponDTO getCouponById(Long id);

    CouponDTO updateCoupon(Long id, CouponDTO couponDTO);

    void deleteCoupon(Long id);

    ApplicableCouponsResponse getApplicableCoupons(CartRequest request);

    ApplyCouponResponse applyCoupon(Long couponId, CartRequest request);

}
