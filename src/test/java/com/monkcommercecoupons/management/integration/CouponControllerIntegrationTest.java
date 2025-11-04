package com.monkcommercecoupons.management.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.monkcommercecoupons.management.model.dto.CartDTO;
import com.monkcommercecoupons.management.model.dto.CartItemDTO;
import com.monkcommercecoupons.management.model.dto.CartRequest;
import com.monkcommercecoupons.management.model.dto.CouponDTO;
import com.monkcommercecoupons.management.model.enums.CouponType;
import com.monkcommercecoupons.management.repository.CouponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CouponControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CouponRepository couponRepository;

    @BeforeEach
    void setUp() {
        couponRepository.deleteAll();
    }

    @Test
    void createCoupon_ValidCartWiseCoupon_ShouldReturn201() throws Exception {
        ObjectNode details = objectMapper.createObjectNode();
        details.put("threshold", 100.0);
        details.put("discount", 10.0);
        details.put("discountType", "PERCENTAGE");

        CouponDTO couponDTO = CouponDTO.builder()
                .code("SAVE10")
                .type(CouponType.CART_WISE)
                .description("10% off on orders above ₹100")
                .details(details)
                .isActive(true)
                .build();

        mockMvc.perform(post("/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(couponDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.code").value("SAVE10"))
                .andExpect(jsonPath("$.type").value("CART_WISE"));
    }

    @Test
    void createCoupon_ValidProductWiseCoupon_ShouldReturn201() throws Exception {
        ObjectNode details = objectMapper.createObjectNode();
        details.put("productId", 1);
        details.put("discount", 20.0);
        details.put("discountType", "PERCENTAGE");

        CouponDTO couponDTO = CouponDTO.builder()
                .code("PRODUCT20")
                .type(CouponType.PRODUCT_WISE)
                .description("20% off on Product 1")
                .details(details)
                .isActive(true)
                .build();

        mockMvc.perform(post("/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(couponDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("PRODUCT20"))
                .andExpect(jsonPath("$.type").value("PRODUCT_WISE"));
    }

    @Test
    void createCoupon_ValidBxGyCoupon_ShouldReturn201() throws Exception {
        ObjectNode details = objectMapper.createObjectNode();

        ArrayNode buyProducts = objectMapper.createArrayNode();
        ObjectNode buy1 = objectMapper.createObjectNode();
        buy1.put("productId", 1);
        buy1.put("quantity", 2);
        buyProducts.add(buy1);

        ArrayNode getProducts = objectMapper.createArrayNode();
        ObjectNode get1 = objectMapper.createObjectNode();
        get1.put("productId", 3);
        get1.put("quantity", 1);
        getProducts.add(get1);

        details.set("buyProducts", buyProducts);
        details.set("getProducts", getProducts);
        details.put("repetitionLimit", 2);

        CouponDTO couponDTO = CouponDTO.builder()
                .code("B2G1")
                .type(CouponType.BXGY)
                .description("Buy 2 get 1 free")
                .details(details)
                .isActive(true)
                .build();

        mockMvc.perform(post("/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(couponDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("B2G1"))
                .andExpect(jsonPath("$.type").value("BXGY"));
    }

    @Test
    void createCoupon_DuplicateCode_ShouldReturn409() throws Exception {
        ObjectNode details = objectMapper.createObjectNode();
        details.put("threshold", 100.0);
        details.put("discount", 10.0);

        CouponDTO couponDTO = CouponDTO.builder()
                .code("DUPLICATE")
                .type(CouponType.CART_WISE)
                .details(details)
                .isActive(true)
                .build();

        mockMvc.perform(post("/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(couponDTO)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(couponDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("DUPLICATE")));
    }

    @Test
    void getAllCoupons_ShouldReturnAllCoupons() throws Exception {
        createTestCoupon("SAVE10", CouponType.CART_WISE);
        createTestCoupon("PRODUCT20", CouponType.PRODUCT_WISE);

        mockMvc.perform(get("/coupons"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].code").exists())
                .andExpect(jsonPath("$[1].code").exists());
    }

    @Test
    void getCouponById_ExistingCoupon_ShouldReturn200() throws Exception {
        MvcResult result = createTestCoupon("SAVE10", CouponType.CART_WISE);
        String response = result.getResponse().getContentAsString();
        Long couponId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/coupons/" + couponId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(couponId))
                .andExpect(jsonPath("$.code").value("SAVE10"));
    }

    @Test
    void getCouponById_NonExistingCoupon_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/coupons/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("999")));
    }

    @Test
    void updateCoupon_ExistingCoupon_ShouldReturn200() throws Exception {
        MvcResult result = createTestCoupon("SAVE10", CouponType.CART_WISE);
        String response = result.getResponse().getContentAsString();
        CouponDTO existingCoupon = objectMapper.readValue(response, CouponDTO.class);
        Long couponId = existingCoupon.getId();

        // Update the existing coupon object
        existingCoupon.setDescription("Updated description");
        existingCoupon.setIsActive(false);

        mockMvc.perform(put("/coupons/" + couponId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(existingCoupon)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(couponId))
                .andExpect(jsonPath("$.isActive").value(false));
    }

    @Test
    void deleteCoupon_ExistingCoupon_ShouldReturn204() throws Exception {
        MvcResult result = createTestCoupon("SAVE10", CouponType.CART_WISE);
        String response = result.getResponse().getContentAsString();
        Long couponId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/coupons/" + couponId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/coupons/" + couponId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getApplicableCoupons_WithValidCart_ShouldReturnApplicableCoupons() throws Exception {
        createTestCoupon("SAVE10", CouponType.CART_WISE);

        CartDTO cart = CartDTO.builder()
                .items(List.of(
                        CartItemDTO.builder()
                                .productId(1L)
                                .quantity(2)
                                .price(60.0)
                                .build()
                ))
                .build();

        Map<String, Object> request = new HashMap<>();
        request.put("cart", cart);

        mockMvc.perform(post("/applicable-coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicable_coupons").isArray())
                .andExpect(jsonPath("$.applicable_coupons[0].code").value("SAVE10"))
                .andExpect(jsonPath("$.applicable_coupons[0].discount").exists());
    }

    @Test
    void applyCoupon_ValidCartWiseCoupon_ShouldReturnUpdatedCart() throws Exception {
        MvcResult result = createTestCoupon("SAVE10", CouponType.CART_WISE);
        String response = result.getResponse().getContentAsString();
        Long couponId = objectMapper.readTree(response).get("id").asLong();

        CartDTO cart = CartDTO.builder()
                .items(List.of(
                        CartItemDTO.builder()
                                .productId(1L)
                                .quantity(2)
                                .price(60.0)
                                .build()
                ))
                .build();

        Map<String, Object> request = new HashMap<>();
        request.put("cart", cart);

        mockMvc.perform(post("/apply-coupon/" + couponId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.updated_cart").exists())
                .andExpect(jsonPath("$.updated_cart.total_price").value(120.0))
                .andExpect(jsonPath("$.updated_cart.total_discount").value(12.0))
                .andExpect(jsonPath("$.updated_cart.final_price").value(108.0));
    }

    @Test
    void applyCoupon_InvalidCouponId_ShouldReturn404() throws Exception {
        CartDTO cart = CartDTO.builder()
                .items(List.of(
                        CartItemDTO.builder()
                                .productId(1L)
                                .quantity(2)
                                .price(60.0)
                                .build()
                ))
                .build();

        Map<String, Object> request = new HashMap<>();
        request.put("cart", cart);

        mockMvc.perform(post("/apply-coupon/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void applyCoupon_CouponNotApplicable_ShouldReturn400() throws Exception {
        // Create coupon with threshold 500
        ObjectNode details = objectMapper.createObjectNode();
        details.put("threshold", 500.0);
        details.put("discount", 10.0);

        CouponDTO couponDTO = CouponDTO.builder()
                .code("HIGHTHRESHOLD")
                .type(CouponType.CART_WISE)
                .details(details)
                .isActive(true)
                .build();

        MvcResult result = mockMvc.perform(post("/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(couponDTO)))
                .andExpect(status().isCreated())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        Long couponId = objectMapper.readTree(response).get("id").asLong();

        // Cart with low value
        CartDTO cart = CartDTO.builder()
                .items(List.of(
                        CartItemDTO.builder()
                                .productId(1L)
                                .quantity(1)
                                .price(50.0)
                                .build()
                ))
                .build();

        Map<String, Object> request = new HashMap<>();
        request.put("cart", cart);

        mockMvc.perform(post("/apply-coupon/" + couponId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("threshold")));
    }

    private MvcResult createTestCoupon(String code, CouponType type) throws Exception {
        ObjectNode details = objectMapper.createObjectNode();

        if (type == CouponType.CART_WISE) {
            details.put("threshold", 100.0);
            details.put("discount", 10.0);
            details.put("discountType", "PERCENTAGE");
        } else if (type == CouponType.PRODUCT_WISE) {
            details.put("productId", 1);
            details.put("discount", 20.0);
            details.put("discountType", "PERCENTAGE");
        }

        CouponDTO couponDTO = CouponDTO.builder()
                .code(code)
                .type(type)
                .description("Test coupon")
                .details(details)
                .isActive(true)
                .build();

        return mockMvc.perform(post("/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(couponDTO)))
                .andExpect(status().isCreated())
                .andReturn();
    }
}
