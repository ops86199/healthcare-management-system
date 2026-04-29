package com.healthcare.auth.service;

import com.healthcare.auth.dto.LoginRequest;
import com.healthcare.auth.dto.LoginResponse;
import com.healthcare.auth.dto.RegisterRequest;

public interface AuthService {

    /**
     * Registers a new user in the system.
     *
     * @param request the registration request containing user details
     * @return LoginResponse with JWT token and user info
     */
    LoginResponse register(RegisterRequest request);

    /**
     * Authenticates an existing user and returns a JWT token.
     *
     * @param request the login request containing credentials
     * @return LoginResponse with JWT token and user info
     */
    LoginResponse login(LoginRequest request);
}
