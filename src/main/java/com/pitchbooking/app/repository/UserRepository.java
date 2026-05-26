package com.pitchbooking.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.pitchbooking.app.domain.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findUserById(long id);

    boolean existsByEmail(String email);

    User findByEmail(String email);

}
