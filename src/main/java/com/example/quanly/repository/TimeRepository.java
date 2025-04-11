package com.example.quanly.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.example.quanly.domain.AvailableTime;


public interface TimeRepository extends JpaRepository<AvailableTime, Long>{

    AvailableTime getById(long timeId);


} 