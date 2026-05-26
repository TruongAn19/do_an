package com.pitchbooking.app.domain.utility;

import com.pitchbooking.app.domain.User;

public interface UserDAO {
    User findByEmail(String email);
    void update(User user);
}
