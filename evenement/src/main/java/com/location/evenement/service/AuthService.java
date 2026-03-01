package com.location.evenement.service;

import com.location.evenement.dto.request.ForgotPasswordRequest;
import com.location.evenement.dto.request.LoginRequest;
import com.location.evenement.dto.request.RegisterRequest;
import com.location.evenement.dto.request.ResetPasswordRequest;
import com.location.evenement.dto.response.JwtResponse;
import com.location.evenement.dto.response.MessageResponse;
import com.location.evenement.dto.response.UserResponse;

public interface AuthService {
    JwtResponse login(LoginRequest request);
    UserResponse register(RegisterRequest request);
    MessageResponse forgotPassword(ForgotPasswordRequest request);
    MessageResponse resetPassword(ResetPasswordRequest request);
}