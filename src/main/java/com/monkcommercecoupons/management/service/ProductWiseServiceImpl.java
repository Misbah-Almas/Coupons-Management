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
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
public class ProductWiseServiceImpl implements ProductWiseService{

    private final ObjectMapper objectMapper;

    @Override
    public double calculateDiscount(Coupon coupon, CartDTO cart) {
        try {
            JsonNode details = objectMapper.readTree(coupon.getDetails());

            long productId = details.get("productId").asLong();
            double discount = details.get("discount").asDouble();
            String discountType = details.has("discountType") ?
                    details.get("discountType").asText() : "PERCENTAGE";

            Optional<CartItemDTO> targetItem = cart.getItems().stream()
                    .filter(item -> item.getProductId().equals(productId))
                    .findFirst();

            if (targetItem.isEmpty()) {
                throw new CouponNotApplicableException(
                        "Product with ID " + productId + " not found in cart");
            }

            CartItemDTO item = targetItem.get();

            if (details.has("minQuantity")) {
                int minQuantity = details.get("minQuantity").asInt();
                if (item.getQuantity() < minQuantity) {
                    throw new CouponNotApplicableException(
                            "Product quantity " + item.getQuantity() +
                                    " is below minimum required: " + minQuantity);
                }
            }

            double itemTotal = item.getTotalPrice();
            double calculatedDiscount;

            if ("PERCENTAGE".equals(discountType)) {
                calculatedDiscount = (itemTotal * discount) / 100.0;
            } else {
                calculatedDiscount = discount * item.getQuantity();
            }

            if (details.has("maxDiscount")) {
                double maxDiscount = details.get("maxDiscount").asDouble();
                calculatedDiscount = Math.min(calculatedDiscount, maxDiscount);
            }

            calculatedDiscount = Math.min(calculatedDiscount, itemTotal);

            return calculatedDiscount;

        } catch (CouponNotApplicableException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error calculating product-wise discount", e);
            throw new RuntimeException("Error processing coupon details", e);
        }
    }

    @Override
    public UpdatedCartDTO applyDiscount(Coupon coupon, CartDTO cart) {
        try {
            JsonNode details = objectMapper.readTree(coupon.getDetails());
            long productId = details.get("productId").asLong();

            double totalDiscount = calculateDiscount(coupon, cart);
            double totalPrice = cart.getTotalPrice();
            double finalPrice = totalPrice - totalDiscount;

            List<CartItemDTO> updatedItems = new ArrayList<>();

            for (CartItemDTO item : cart.getItems()) {
                CartItemDTO updatedItem = CartItemDTO.builder()
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .totalDiscount(item.getProductId().equals(productId) ?
                                totalDiscount : 0.0)
                        .build();

                updatedItems.add(updatedItem);
            }

            return UpdatedCartDTO.builder()
                    .items(updatedItems)
                    .totalPrice(Math.round(totalPrice * 100.0) / 100.0)
                    .totalDiscount(Math.round(totalDiscount * 100.0) / 100.0)
                    .finalPrice(Math.round(finalPrice * 100.0) / 100.0)
                    .build();

        } catch (Exception e) {
            log.error("Error applying product-wise discount", e);
            throw new RuntimeException("Error processing coupon", e);
        }
    }
}
