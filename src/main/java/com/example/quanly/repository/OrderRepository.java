package com.example.quanly.repository;

import java.util.List;


// import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.quanly.domain.Order;
import com.example.quanly.domain.User;


@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    // Order getOrderById(long orderId);
    List<Order> findByUser(User user);
}
