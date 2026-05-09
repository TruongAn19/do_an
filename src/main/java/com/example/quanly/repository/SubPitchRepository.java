package com.example.quanly.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.quanly.domain.Product;
import com.example.quanly.domain.SubPitch;

@Repository
public interface SubPitchRepository extends JpaRepository<SubPitch, Long> {
    List<SubPitch> findByProduct(Product product);

    List<SubPitch> findByProductId(Long productId);
}
