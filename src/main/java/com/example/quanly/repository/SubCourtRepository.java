package com.example.quanly.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.quanly.domain.Product;
import com.example.quanly.domain.SubCourt;

@Repository
public interface SubCourtRepository extends JpaRepository<SubCourt, Long> {
    List<SubCourt> findByProduct(Product product);

    List<SubCourt> findByProductId(Long productId);

    @Query("""
            SELECT sc
            FROM SubCourt sc
            JOIN FETCH sc.product p
            WHERE p.status IS NULL OR p.status <> 'DELETED'
            ORDER BY p.name, sc.name
            """)
    List<SubCourt> findActiveCourts();
}
