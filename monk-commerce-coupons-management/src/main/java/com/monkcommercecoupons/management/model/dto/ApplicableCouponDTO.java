package com.monkcommercecoupons.management.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.monkcommercecoupons.management.model.enums.CouponType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicableCouponDTO {

    @JsonProperty("coupon_id")
    private Long couponId;

    private String code;

    private CouponType type;

    private Double discount;

    private String description;
}
