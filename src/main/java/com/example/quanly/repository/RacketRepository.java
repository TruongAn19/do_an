package com.example.quanly.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.example.quanly.domain.Product;
import com.example.quanly.domain.Racket;

@Repository
public interface RacketRepository extends JpaRepository<Racket, Long>, JpaSpecificationExecutor<Racket>{
    List<Racket> findByProductAndAvailableTrue(Product product);
    Page<Racket> findByNameContainingIgnoreCase(String name, Pageable pageable);

}
