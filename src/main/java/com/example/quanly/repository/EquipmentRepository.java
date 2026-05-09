package com.example.quanly.repository;

import java.util.List;

import com.example.quanly.domain.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long>, JpaSpecificationExecutor<Equipment> {

    @Query("SELECT r FROM Equipment r WHERE r.product.id = :productId AND r.available = true")
    List<Equipment> findByProductAndAvailableTrue(Long productId);

    @Query("SELECT Sum(r.quantity) FROM Equipment r WHERE r.product.id = :productId AND r.available = true")
    Integer countEquipmentByProductId(Long productId);

    @Query("SELECT sum(r.quantity) FROM Equipment r")
    Integer countRackeQuantity();

}
