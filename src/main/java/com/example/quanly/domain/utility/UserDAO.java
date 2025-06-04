package com.example.quanly.domain.utility;

import com.example.quanly.domain.User;

public interface UserDAO {
    User findByEmail(String email);
    void update(User user);
}
