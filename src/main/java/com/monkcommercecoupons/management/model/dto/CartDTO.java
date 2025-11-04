package com.monkcommercecoupons.management.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartDTO {

    @NotEmpty(message = "Cart items cannot be empty")
    @Valid
    private List<CartItemDTO> items;

    @JsonIgnore
    public double getTotalPrice() {
        return items.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
    }

    @JsonIgnore
    public int getTotalItems() {
        return items.stream()
                .mapToInt(CartItemDTO::getQuantity)
                .sum();
    }
}
