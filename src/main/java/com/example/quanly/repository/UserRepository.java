package com.example.quanly.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.quanly.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findUserById(long id);

    boolean existsByEmail(String email);

    User findByEmail(String email);

    Page<User> findByEnabledTrue(Pageable pageable);

}
