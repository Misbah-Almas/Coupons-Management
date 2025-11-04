package com.monkcommercecoupons.management.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.monkcommercecoupons.management.exception.CouponNotApplicableException;
import com.monkcommercecoupons.management.model.dto.CartDTO;
import com.monkcommercecoupons.management.model.dto.CartItemDTO;
import com.monkcommercecoupons.management.model.dto.UpdatedCartDTO;
import com.monkcommercecoupons.management.model.entity.Coupon;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BxGyServiceImpl implements BxGyService {

    private final ObjectMapper objectMapper;

    @Override
    public double calculateDiscount(Coupon coupon, CartDTO cart) {
        try {
            JsonNode details = objectMapper.readTree(coupon.getDetails());

            List<ProductQuantity> buyProducts = parseProductList(details.get("buyProducts"));
            List<ProductQuantity> getProducts = parseProductList(details.get("getProducts"));
            int repetitionLimit = details.has("repetitionLimit") ?
                    details.get("repetitionLimit").asInt() : 1;

            int totalBuyQty = buyProducts.stream()
                    .mapToInt(ProductQuantity::getQuantity)
                    .sum();

            Map<Long, Integer> cartProductQty = cart.getItems().stream()
                    .collect(Collectors.toMap(
                            CartItemDTO::getProductId,
                            CartItemDTO::getQuantity
                    ));

            int availableBuyQty = buyProducts.stream()
                    .mapToInt(pq -> cartProductQty.getOrDefault(pq.getProductId(), 0))
                    .sum();

            if (availableBuyQty < totalBuyQty) {
                throw new CouponNotApplicableException(
                        "Insufficient buy products. Required: " + totalBuyQty + ", Available: " + availableBuyQty);
            }

            int possibleRepetitions = availableBuyQty / totalBuyQty;
            possibleRepetitions = Math.min(possibleRepetitions, repetitionLimit);

            int totalGetQty = getProducts.stream()
                    .mapToInt(ProductQuantity::getQuantity)
                    .sum();

            int totalFreeQty = totalGetQty * possibleRepetitions;

            List<CartItemDTO> availableGetItems = cart.getItems().stream()
                    .filter(item -> getProducts.stream()
                            .anyMatch(gp -> gp.getProductId().equals(item.getProductId())))
                    .sorted(Comparator.comparingDouble(CartItemDTO::getPrice))
                    .toList();

            if (availableGetItems.isEmpty()) {
                throw new CouponNotApplicableException(
                        "None of the 'get' products are in the cart");
            }

            double totalDiscount = 0.0;
            int remainingFreeQty = totalFreeQty;

            for (CartItemDTO item : availableGetItems) {
                if (remainingFreeQty <= 0) break;

                int freeQtyForItem = Math.min(remainingFreeQty, item.getQuantity());
                double itemDiscount = freeQtyForItem * item.getPrice();
                totalDiscount += itemDiscount;
                remainingFreeQty -= freeQtyForItem;
            }

            return totalDiscount;

        } catch (CouponNotApplicableException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error calculating BxGy discount", e);
            throw new RuntimeException("Error processing coupon details", e);
        }
    }

    @Override
    public UpdatedCartDTO applyDiscount(Coupon coupon, CartDTO cart) {
        try {
            JsonNode details = objectMapper.readTree(coupon.getDetails());

            List<ProductQuantity> buyProducts = parseProductList(details.get("buyProducts"));
            List<ProductQuantity> getProducts = parseProductList(details.get("getProducts"));
            int repetitionLimit = details.has("repetitionLimit") ?
                    details.get("repetitionLimit").asInt() : 1;

            Map<Long, Integer> cartProductQty = cart.getItems().stream()
                    .collect(Collectors.toMap(
                            CartItemDTO::getProductId,
                            CartItemDTO::getQuantity
                    ));

            int totalBuyQty = buyProducts.stream()
                    .mapToInt(ProductQuantity::getQuantity)
                    .sum();

            int availableBuyQty = buyProducts.stream()
                    .mapToInt(pq -> cartProductQty.getOrDefault(pq.getProductId(), 0))
                    .sum();

            int possibleRepetitions = Math.min(availableBuyQty / totalBuyQty, repetitionLimit);

            int totalGetQty = getProducts.stream()
                    .mapToInt(ProductQuantity::getQuantity)
                    .sum();

            int totalFreeQty = totalGetQty * possibleRepetitions;

            List<CartItemDTO> updatedItems = new ArrayList<>();
            Set<Long> getProductIds = getProducts.stream()
                    .map(ProductQuantity::getProductId)
                    .collect(Collectors.toSet());

            List<CartItemDTO> sortedGetItems = cart.getItems().stream()
                    .filter(item -> getProductIds.contains(item.getProductId()))
                    .sorted(Comparator.comparingDouble(CartItemDTO::getPrice))
                    .toList();

            double totalDiscount = 0.0;
            int remainingFreeQty = totalFreeQty;
            Map<Long, Double> discountPerProduct = new HashMap<>();

            for (CartItemDTO item : sortedGetItems) {
                if (remainingFreeQty <= 0) break;

                int freeQtyForItem = Math.min(remainingFreeQty, item.getQuantity());
                double itemDiscount = freeQtyForItem * item.getPrice();
                discountPerProduct.put(item.getProductId(), itemDiscount);
                totalDiscount += itemDiscount;
                remainingFreeQty -= freeQtyForItem;
            }

            for (CartItemDTO item : cart.getItems()) {
                CartItemDTO updatedItem = CartItemDTO.builder()
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .totalDiscount(discountPerProduct.getOrDefault(item.getProductId(), 0.0))
                        .build();

                updatedItems.add(updatedItem);
            }

            double totalPrice = cart.getTotalPrice();
            double finalPrice = totalPrice - totalDiscount;

            return UpdatedCartDTO.builder()
                    .items(updatedItems)
                    .totalPrice(Math.round(totalPrice * 100.0) / 100.0)
                    .totalDiscount(Math.round(totalDiscount * 100.0) / 100.0)
                    .finalPrice(Math.round(finalPrice * 100.0) / 100.0)
                    .build();

        } catch (Exception e) {
            log.error("Error applying BxGy discount", e);
            throw new RuntimeException("Error processing coupon", e);
        }
    }

    private List<ProductQuantity> parseProductList(JsonNode productsNode) {
        List<ProductQuantity> products = new ArrayList<>();
        if (productsNode != null && productsNode.isArray()) {
            for (JsonNode node : productsNode) {
                products.add(new ProductQuantity(
                        node.get("productId").asLong(),
                        node.get("quantity").asInt()
                ));
            }
        }
        return products;
    }

    @Data
    @AllArgsConstructor
    private static class ProductQuantity {
        private Long productId;
        private Integer quantity;
    }
}
