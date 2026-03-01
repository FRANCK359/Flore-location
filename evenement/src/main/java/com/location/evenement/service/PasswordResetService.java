package com.location.evenement.service;

import com.location.evenement.dto.request.ForgotPasswordRequest;
import com.location.evenement.dto.request.ResetPasswordRequest;
import com.location.evenement.dto.response.MessageResponse;

public interface PasswordResetService {
    MessageResponse forgotPassword(ForgotPasswordRequest request);
    MessageResponse resetPassword(ResetPasswordRequest request);
}