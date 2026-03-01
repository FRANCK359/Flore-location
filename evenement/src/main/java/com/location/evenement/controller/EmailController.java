package com.location.evenement.controller;

import com.location.evenement.dto.request.EmailRequest;
import com.location.evenement.dto.response.EmailResponse;
import com.location.evenement.model.User;
import com.location.evenement.service.EmailService;
import com.location.evenement.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/emails")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;
    private final UserService userService;

    @PostMapping("/send")
    public ResponseEntity<EmailResponse> sendEmail(@Valid @RequestBody EmailRequest request) {
        EmailResponse response;
        if (request.isHtml()) {
            response = emailService.sendHtmlEmail(request);
        } else {
            response = emailService.sendSimpleEmail(request);
        }

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/welcome/{userId}")
    public ResponseEntity<EmailResponse> sendWelcomeEmail(@PathVariable Long userId) {
        User user = userService.getUserEntityById(userId);
        EmailResponse response = emailService.sendWelcomeEmail(user);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/test")
    public ResponseEntity<String> testEmail(@RequestParam String to) {
        EmailRequest request = new EmailRequest();
        request.setTo(to);
        request.setSubject("Test Email");
        request.setContent("Ceci est un email de test.");
        request.setHtml(false);

        EmailResponse response = emailService.sendSimpleEmail(request);

        if (response.isSuccess()) {
            return ResponseEntity.ok("Email de test envoyé avec succès à " + to);
        } else {
            return ResponseEntity.badRequest().body("Erreur: " + response.getErrorMessage());
        }
    }
}