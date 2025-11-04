package com.monkcommercecoupons.management.service;

import com.monkcommercecoupons.management.exception.CouponNotApplicableException;
import com.monkcommercecoupons.management.model.dto.CartDTO;
import com.monkcommercecoupons.management.model.dto.CartItemDTO;
import com.monkcommercecoupons.management.model.dto.UpdatedCartDTO;
import com.monkcommercecoupons.management.model.entity.Coupon;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartWiseServiceImpl implements CartWiseService{

    private final ObjectMapper objectMapper;

    @Override
    public double calculateDiscount(Coupon coupon, CartDTO cart) {
        try {
            JsonNode details = objectMapper.readTree(coupon.getDetails());

            double threshold = details.get("threshold").asDouble();
            double discount = details.get("discount").asDouble();
            String discountType = details.has("discountType") ?
                    details.get("discountType").asText() : "PERCENTAGE";

            double cartTotal = cart.getTotalPrice();

            if (cartTotal < threshold) {
                throw new CouponNotApplicableException(
                        "Cart total ₹" + cartTotal + " is below threshold ₹" + threshold);
            }

            if (details.has("minItems")) {
                int minItems = details.get("minItems").asInt();
                if (cart.getTotalItems() < minItems) {
                    throw new CouponNotApplicableException(
                            "Cart has " + cart.getTotalItems() + " items, minimum required: " + minItems);
                }
            }

            double calculatedDiscount;
            if ("PERCENTAGE".equals(discountType)) {
                calculatedDiscount = (cartTotal * discount) / 100.0;
            } else {
                calculatedDiscount = discount;
            }

            if (details.has("maxDiscount")) {
                double maxDiscount = details.get("maxDiscount").asDouble();
                calculatedDiscount = Math.min(calculatedDiscount, maxDiscount);
            }

            calculatedDiscount = Math.min(calculatedDiscount, cartTotal);

            return calculatedDiscount;

        } catch (CouponNotApplicableException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error calculating cart-wise discount", e);
            throw new RuntimeException("Error processing coupon details", e);
        }
    }

    @Override
    public UpdatedCartDTO applyDiscount(Coupon coupon, CartDTO cart) {
        double totalDiscount = calculateDiscount(coupon, cart);
        double totalPrice = cart.getTotalPrice();
        double finalPrice = totalPrice - totalDiscount;

        List<CartItemDTO> updatedItems = new ArrayList<>();
        double remainingDiscount = totalDiscount;

        for (int i = 0; i < cart.getItems().size(); i++) {
            CartItemDTO item = cart.getItems().get(i);
            double itemTotal = item.getTotalPrice();
            double itemDiscount;

            if (i == cart.getItems().size() - 1) {
                itemDiscount = remainingDiscount;
            } else {
                itemDiscount = (itemTotal / totalPrice) * totalDiscount;
                itemDiscount = Math.round(itemDiscount * 100.0) / 100.0;
            }

            remainingDiscount -= itemDiscount;

            CartItemDTO updatedItem = CartItemDTO.builder()
                    .productId(item.getProductId())
                    .quantity(item.getQuantity())
                    .price(item.getPrice())
                    .totalDiscount(itemDiscount)
                    .build();

            updatedItems.add(updatedItem);
        }

        return UpdatedCartDTO.builder()
                .items(updatedItems)
                .totalPrice(Math.round(totalPrice * 100.0) / 100.0)
                .totalDiscount(Math.round(totalDiscount * 100.0) / 100.0)
                .finalPrice(Math.round(finalPrice * 100.0) / 100.0)
                .build();
    }
}
