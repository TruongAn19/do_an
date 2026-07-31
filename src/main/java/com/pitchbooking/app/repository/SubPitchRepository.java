package com.pitchbooking.app.repository;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pitchbooking.app.domain.PitchType;
import com.pitchbooking.app.domain.Product;
import com.pitchbooking.app.domain.SubPitch;

@Repository
public interface SubPitchRepository extends JpaRepository<SubPitch, Long> {
    List<SubPitch> findByProduct(Product product);

    List<SubPitch> findByProductId(Long productId);

    List<SubPitch> findByProductIdAndPitchType(Long productId, PitchType pitchType);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT sp FROM SubPitch sp WHERE sp.id = :id")
    Optional<SubPitch> findByIdWithLock(@Param("id") Long id);
}
