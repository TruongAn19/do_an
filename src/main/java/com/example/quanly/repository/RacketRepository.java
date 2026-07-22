package com.example.quanly.repository;

import java.util.List;
import java.util.Optional;

import com.example.quanly.domain.Racket;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RacketRepository extends JpaRepository<Racket, Long>, JpaSpecificationExecutor<Racket> {

    @Query("SELECT r FROM Racket r WHERE r.product.id = :productId AND r.available = true")
    List<Racket> findByProductAndAvailableTrue(Long productId);

    List<Racket> findByProductId(Long productId);

    /**
     * A2: lấy Racket có khoá ghi bi quan (SELECT ... FOR UPDATE) để re-check + trừ tồn kho
     * lúc confirm thanh toán. Hai callback confirm song song trên cùng vợt sẽ bị serial hoá,
     * tránh overbook / stock âm trong khoảng prepare→confirm (tới 18 phút).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Racket r WHERE r.id = :id")
    Optional<Racket> findByIdForUpdate(@Param("id") Long id);

    // productId nullable: null = sum trên tất cả sân (dashboard tổng quan).
    @Query("SELECT Sum(r.quantity) FROM Racket r WHERE r.available = true AND (:productId IS NULL OR r.product.id = :productId)")
    Integer countRacketByProductId(Long productId);

    @Query("SELECT sum(r.quantity) FROM Racket r")
    Integer countRackeQuantity();

}
