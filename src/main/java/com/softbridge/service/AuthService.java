package com.softbridge.service;

import com.softbridge.dto.request.LoginRequest;
import com.softbridge.dto.request.RegisterRequest;
import com.softbridge.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
