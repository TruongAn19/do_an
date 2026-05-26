package com.pitchbooking.app.domain.utility;

import com.pitchbooking.app.domain.PasswordResetToken;
import com.pitchbooking.app.domain.User;

public interface PasswordResetTokenDAO {
    void save(PasswordResetToken token);
    PasswordResetToken findByToken(String token);
    void delete(PasswordResetToken token);
    PasswordResetToken findByUser(User user);

}
