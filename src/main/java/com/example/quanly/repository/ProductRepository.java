package com.example.quanly.repository;

import org.springframework.data.domain.Page;
 import org.springframework.data.domain.Pageable;
 import org.springframework.data.jpa.repository.JpaRepository;
 import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.quanly.domain.Product;
@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product>{

    @Query("SELECT p FROM Product p WHERE p.address IS NOT NULL AND p.address <> ''")
    Page<Product> findAllMainProducts(Pageable page);

    @Query("SELECT p FROM Product p WHERE p.address IS NULL OR p.address = ''")
    Page<Product> findAllByProducts(Pageable page);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.address IS NOT NULL AND LENGTH(TRIM(p.address)) > 0")
    long countMainProduct();
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.address IS NULL OR LENGTH(TRIM(p.address)) = 0")
    long countByProduct();
    
    Product getById(long id);
    
}