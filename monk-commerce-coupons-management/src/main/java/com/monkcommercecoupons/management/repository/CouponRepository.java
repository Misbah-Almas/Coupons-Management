package com.monkcommercecoupons.management.repository;

import com.monkcommercecoupons.management.model.entity.Coupon;
import com.monkcommercecoupons.management.model.enums.CouponType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByCode(String code);

    boolean existsByCode(String code);

    List<Coupon> findByType(CouponType type);

    @Query("SELECT c FROM Coupon c WHERE c.isActive = true AND (c.expirationDate IS NULL OR c.expirationDate > :now)")
    List<Coupon> findAllActiveCoupons(LocalDateTime now);

    @Query("SELECT c FROM Coupon c WHERE c.isActive = true AND c.type = :type AND (c.expirationDate IS NULL OR c.expirationDate > :now)")
    List<Coupon> findActiveByType(CouponType type, LocalDateTime now);
}
