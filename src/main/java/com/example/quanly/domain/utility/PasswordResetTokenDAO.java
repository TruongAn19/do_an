package com.example.quanly.domain.utility;

import com.example.quanly.domain.PasswordResetToken;
import com.example.quanly.domain.User;

public interface PasswordResetTokenDAO {
    void save(PasswordResetToken token);
    PasswordResetToken findByToken(String token);
    void delete(PasswordResetToken token);
    PasswordResetToken findByUser(User user);

}
