package com.pitchbooking.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pitchbooking.app.domain.Product;
import com.pitchbooking.app.domain.SubPitch;

@Repository
public interface SubPitchRepository extends JpaRepository<SubPitch, Long> {
    List<SubPitch> findByProduct(Product product);

    List<SubPitch> findByProductId(Long productId);
}
