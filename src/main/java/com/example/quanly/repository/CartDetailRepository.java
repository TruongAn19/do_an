package com.example.quanly.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.quanly.domain.Cart;
import com.example.quanly.domain.CartDetail;
import com.example.quanly.domain.Product;

@Repository
public interface CartDetailRepository extends JpaRepository<CartDetail, Long>{

    boolean existsByCartAndProduct(Cart cart, Product product);
    CartDetail findByCartAndProduct(Cart cart, Product product);
}