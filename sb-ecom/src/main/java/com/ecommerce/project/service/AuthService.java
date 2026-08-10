package com.ecommerce.project.service;

import com.ecommerce.project.payload.AuthenticationResult;
import com.ecommerce.project.payload.UserResponse;
import com.ecommerce.project.security.request.LoginRequest;
import com.ecommerce.project.security.request.SignupRequest;
import com.ecommerce.project.security.response.MessageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseCookie;

public interface AuthService {
    AuthenticationResult login(LoginRequest loginRequest);

    MessageResponse register(SignupRequest signUpRequest);
   UserResponse getAllSellers(Pageable pageable);
    ResponseCookie logout();
}
