package com.monkcommercecoupons.management.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplyCouponResponse {

    @JsonProperty("updated_cart")
    private UpdatedCartDTO updatedCart;
}
