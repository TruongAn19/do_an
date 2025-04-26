package com.example.quanly.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.quanly.domain.Product;
import com.example.quanly.domain.SubCourt;



@Repository
public interface SubCourtRepository extends JpaRepository<SubCourt, Long> {
    List<SubCourt> findByProduct(Product product);
    
} 
