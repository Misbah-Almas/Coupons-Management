package com.monkcommercecoupons.management.exception;

public class DuplicateCouponCodeException extends RuntimeException {
    public DuplicateCouponCodeException(String code) {
        super("Coupon with code '" + code + "' already exists");
    }
}
