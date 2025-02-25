package com.example.quanly.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.quanly.domain.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long>{

    Role findByName(String name);
}
