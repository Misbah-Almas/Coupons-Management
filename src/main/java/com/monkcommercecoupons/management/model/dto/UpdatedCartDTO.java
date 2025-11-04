package com.monkcommercecoupons.management.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatedCartDTO {

    private List<CartItemDTO> items;

    @JsonProperty("total_price")
    private Double totalPrice;

    @JsonProperty("total_discount")
    private Double totalDiscount;

    @JsonProperty("final_price")
    private Double finalPrice;
}
