package com.pitchbooking.app.repository;

import com.pitchbooking.app.domain.User;

public interface IAuthenticationFacade {
    User getCurrentUser();
}
