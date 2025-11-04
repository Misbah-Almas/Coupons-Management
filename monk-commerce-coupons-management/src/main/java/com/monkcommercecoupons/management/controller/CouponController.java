package com.monkcommercecoupons.management.controller;

import com.monkcommercecoupons.management.model.dto.ApplicableCouponsResponse;
import com.monkcommercecoupons.management.model.dto.ApplyCouponResponse;
import com.monkcommercecoupons.management.model.dto.CartRequest;
import com.monkcommercecoupons.management.model.dto.CouponDTO;
import com.monkcommercecoupons.management.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "Coupons", description = "Coupon Management APIs")
public class CouponController {

    private final CouponService couponService;

    @PostMapping("/coupons")
    @Operation(summary = "Create a new coupon", description = "Creates a new discount coupon")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Coupon created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "409", description = "Coupon code already exists")
    })
    public ResponseEntity<CouponDTO> createCoupon(
            @Valid @RequestBody CouponDTO couponDTO) {
        CouponDTO created = couponService.createCoupon(couponDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/coupons")
    @Operation(summary = "Get all coupons", description = "Retrieves all coupons")
    @ApiResponse(responseCode = "200", description = "Coupons retrieved successfully")
    public ResponseEntity<List<CouponDTO>> getAllCoupons() {
        List<CouponDTO> coupons = couponService.getAllCoupons();
        return ResponseEntity.ok(coupons);
    }

    @GetMapping("/coupons/{id}")
    @Operation(summary = "Get coupon by ID", description = "Retrieves a specific coupon by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Coupon found"),
            @ApiResponse(responseCode = "404", description = "Coupon not found")
    })
    public ResponseEntity<CouponDTO> getCouponById(
            @Parameter(description = "Coupon ID") @PathVariable Long id) {
        CouponDTO coupon = couponService.getCouponById(id);
        return ResponseEntity.ok(coupon);
    }

    @PutMapping("/coupons/{id}")
    @Operation(summary = "Update coupon", description = "Updates an existing coupon")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Coupon updated successfully"),
            @ApiResponse(responseCode = "404", description = "Coupon not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<CouponDTO> updateCoupon(
            @Parameter(description = "Coupon ID") @PathVariable Long id,
            @Valid @RequestBody CouponDTO couponDTO) {
        CouponDTO updated = couponService.updateCoupon(id, couponDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/coupons/{id}")
    @Operation(summary = "Delete coupon", description = "Deletes a coupon by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Coupon deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Coupon not found")
    })
    public ResponseEntity<Void> deleteCoupon(
            @Parameter(description = "Coupon ID") @PathVariable Long id) {
        couponService.deleteCoupon(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/applicable-coupons")
    @Operation(summary = "Get applicable coupons",
            description = "Finds all coupons applicable to the given cart")
    @ApiResponse(responseCode = "200", description = "Applicable coupons retrieved")
    public ResponseEntity<ApplicableCouponsResponse> getApplicableCoupons(
            @Valid @RequestBody CartRequest request) {
        ApplicableCouponsResponse response = couponService.getApplicableCoupons(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/apply-coupon/{id}")
    @Operation(summary = "Apply coupon to cart",
            description = "Applies a specific coupon to the cart and returns updated cart")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Coupon applied successfully"),
            @ApiResponse(responseCode = "404", description = "Coupon not found"),
            @ApiResponse(responseCode = "400", description = "Coupon not applicable")
    })
    public ResponseEntity<ApplyCouponResponse> applyCoupon(
            @Parameter(description = "Coupon ID") @PathVariable Long id,
            @Valid @RequestBody CartRequest request) {
        ApplyCouponResponse response = couponService.applyCoupon(id, request);
        return ResponseEntity.ok(response);
    }
}
