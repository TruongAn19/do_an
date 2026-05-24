package com.example.quanly.repository;

import java.util.List;

import com.example.quanly.domain.Racket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RacketRepository extends JpaRepository<Racket, Long>, JpaSpecificationExecutor<Racket> {

    @Query("SELECT r FROM Racket r WHERE r.product.id = :productId AND r.available = true")
    List<Racket> findByProductAndAvailableTrue(Long productId);

    // productId nullable: null = sum trên tất cả sân (dashboard tổng quan).
    @Query("SELECT Sum(r.quantity) FROM Racket r WHERE r.available = true AND (:productId IS NULL OR r.product.id = :productId)")
    Integer countRacketByProductId(Long productId);

    @Query("SELECT sum(r.quantity) FROM Racket r")
    Integer countRackeQuantity();

}
