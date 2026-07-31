package com.example.quanly.repository;

import java.util.List;

import com.example.quanly.domain.Racket;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RacketRepository extends JpaRepository<Racket, Long>, JpaSpecificationExecutor<Racket> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Racket r WHERE r.id = :id")
    Optional<Racket> findByIdForUpdate(@Param("id") Long id);

    @Query("SELECT r FROM Racket r WHERE r.product.id = :productId AND r.available = true")
    List<Racket> findByProductAndAvailableTrue(Long productId);

    @Query("SELECT r FROM Racket r WHERE r.product.id = :productId")
    List<Racket> findByProductId(@Param("productId") Long productId);

    @Query("SELECT Sum(r.quantity) FROM Racket r WHERE r.product.id = :productId AND r.available = true")
    Integer countRacketByProductId(Long productId);

    @Query("SELECT sum(r.quantity) FROM Racket r")
    Integer countRackeQuantity();

}
