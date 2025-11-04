package com.monkcommercecoupons.management.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.monkcommercecoupons.management.exception.CouponNotApplicableException;
import com.monkcommercecoupons.management.exception.CouponNotFoundException;
import com.monkcommercecoupons.management.exception.DuplicateCouponCodeException;
import com.monkcommercecoupons.management.exception.InvalidCouponException;
import com.monkcommercecoupons.management.model.dto.*;
import com.monkcommercecoupons.management.model.entity.Coupon;
import com.monkcommercecoupons.management.model.enums.CouponType;
import com.monkcommercecoupons.management.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class CouponServiceImpl implements CouponService{

    private final CouponRepository couponRepository;
    private final CartWiseService cartWiseService;
    private final ProductWiseService productWiseService;
    private final BxGyService bxGyService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public CouponDTO createCoupon(CouponDTO couponDTO) {
        log.info("Creating coupon with code: {}", couponDTO.getCode());

        if (couponRepository.existsByCode(couponDTO.getCode())) {
            throw new DuplicateCouponCodeException(couponDTO.getCode());
        }

        validateCouponDetails(couponDTO.getType(), couponDTO.getDetails());

        Coupon coupon = Coupon.builder()
                .code(couponDTO.getCode())
                .type(couponDTO.getType())
                .description(couponDTO.getDescription())
                .details(convertDetailsToJson(couponDTO.getDetails()))
                .expirationDate(couponDTO.getExpirationDate())
                .isActive(couponDTO.getIsActive() != null ? couponDTO.getIsActive() : true)
                .build();

        Coupon savedCoupon = couponRepository.save(coupon);
        log.info("Coupon created successfully with id: {}", savedCoupon.getId());

        return convertToDTO(savedCoupon);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CouponDTO> getAllCoupons() {
        log.info("Fetching all coupons");
        return couponRepository.findAll().stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CouponDTO getCouponById(Long id) {
        log.info("Fetching coupon with id: {}", id);
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new CouponNotFoundException(id));
        return convertToDTO(coupon);
    }

    @Override
    @Transactional
    public CouponDTO updateCoupon(Long id, CouponDTO couponDTO) {
        log.info("Updating coupon with id: {}", id);
        Coupon existingCoupon = couponRepository.findById(id)
                .orElseThrow(() -> new CouponNotFoundException(id));

        if (couponDTO.getCode() != null && !couponDTO.getCode().equals(existingCoupon.getCode())) {
            if (couponRepository.existsByCode(couponDTO.getCode())) {
                throw new DuplicateCouponCodeException(couponDTO.getCode());
            }
            existingCoupon.setCode(couponDTO.getCode());
        }

        if (couponDTO.getDescription() != null) {
            existingCoupon.setDescription(couponDTO.getDescription());
        }

        if (couponDTO.getDetails() != null) {
            validateCouponDetails(existingCoupon.getType(), couponDTO.getDetails());
            existingCoupon.setDetails(convertDetailsToJson(couponDTO.getDetails()));
        }

        if (couponDTO.getExpirationDate() != null) {
            existingCoupon.setExpirationDate(couponDTO.getExpirationDate());
        }

        if (couponDTO.getIsActive() != null) {
            existingCoupon.setIsActive(couponDTO.getIsActive());
        }

        Coupon updatedCoupon = couponRepository.save(existingCoupon);
        log.info("Coupon updated successfully with id: {}", updatedCoupon.getId());

        return convertToDTO(updatedCoupon);
    }

    @Override
    @Transactional
    public void deleteCoupon(Long id) {
        log.info("Deleting coupon with id: {}", id);
        if (!couponRepository.existsById(id)) {
            throw new CouponNotFoundException(id);
        }
        couponRepository.deleteById(id);
        log.info("Coupon deleted successfully with id: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicableCouponsResponse getApplicableCoupons(CartRequest request) {
        log.info("Finding applicable coupons for cart with {} items",
                request.getCart().getItems().size());

        List<Coupon> activeCoupons = couponRepository.findAllActiveCoupons(LocalDateTime.now());
        List<ApplicableCouponDTO> applicableCoupons = new ArrayList<>();

        for (Coupon coupon : activeCoupons) {
            try {
                double discount = calculateDiscount(coupon, request.getCart());
                if (discount > 0) {
                    applicableCoupons.add(ApplicableCouponDTO.builder()
                            .couponId(coupon.getId())
                            .code(coupon.getCode())
                            .type(coupon.getType())
                            .discount(Math.round(discount * 100.0) / 100.0)
                            .description(coupon.getDescription())
                            .build());
                }
            } catch (CouponNotApplicableException e) {
                log.debug("Coupon {} not applicable: {}", coupon.getCode(), e.getMessage());
            }
        }

        log.info("Found {} applicable coupons", applicableCoupons.size());
        return ApplicableCouponsResponse.builder()
                .applicableCoupons(applicableCoupons)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ApplyCouponResponse applyCoupon(Long couponId, CartRequest request) {
        log.info("Applying coupon with id: {}", couponId);

        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new CouponNotFoundException(couponId));

        if (!coupon.isValid()) {
            throw new InvalidCouponException("Coupon is either inactive or expired");
        }

        UpdatedCartDTO updatedCart = applyDiscountToCart(coupon, request.getCart());
        log.info("Coupon applied successfully. Final price: {}", updatedCart.getFinalPrice());

        return ApplyCouponResponse.builder()
                .updatedCart(updatedCart)
                .build();
    }

    private double calculateDiscount(Coupon coupon, CartDTO cart) {
        return switch (coupon.getType()) {
            case CART_WISE -> cartWiseService.calculateDiscount(coupon, cart);
            case PRODUCT_WISE -> productWiseService.calculateDiscount(coupon, cart);
            case BXGY -> bxGyService.calculateDiscount(coupon, cart);
        };
    }

    private UpdatedCartDTO applyDiscountToCart(Coupon coupon, CartDTO cart) {
        return switch (coupon.getType()) {
            case CART_WISE -> cartWiseService.applyDiscount(coupon, cart);
            case PRODUCT_WISE -> productWiseService.applyDiscount(coupon, cart);
            case BXGY -> bxGyService.applyDiscount(coupon, cart);
        };
    }

    private void validateCouponDetails(CouponType type, Object details) {
        if (details == null) {
            throw new InvalidCouponException("Coupon details cannot be null");
        }
    }

    private String convertDetailsToJson(Object details) {
        try {
            return objectMapper.writeValueAsString(details);
        } catch (JsonProcessingException e) {
            throw new InvalidCouponException("Invalid coupon details format");
        }
    }

    private CouponDTO convertToDTO(Coupon coupon) {
        try {
            return CouponDTO.builder()
                    .id(coupon.getId())
                    .code(coupon.getCode())
                    .type(coupon.getType())
                    .description(coupon.getDescription())
                    .details(objectMapper.readTree(coupon.getDetails()))
                    .expirationDate(coupon.getExpirationDate())
                    .isActive(coupon.getIsActive())
                    .createdAt(coupon.getCreatedAt())
                    .updatedAt(coupon.getUpdatedAt())
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error parsing coupon details", e);
        }
    }
}
