package com.example.quanly.repository;



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import com.example.quanly.domain.OrderDetail;



@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
    

}
