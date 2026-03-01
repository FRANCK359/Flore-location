package com.location.evenement.service.impl;

import com.location.evenement.dto.request.EmailRequest;
import com.location.evenement.dto.response.EmailResponse;
import com.location.evenement.model.EmailLog;
import com.location.evenement.model.Reservation;
import com.location.evenement.model.User;
import com.location.evenement.repository.EmailLogRepository;
import com.location.evenement.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final EmailLogRepository emailLogRepository;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Override
    public EmailResponse sendSimpleEmail(EmailRequest request) {
        EmailResponse response = new EmailResponse();
        EmailLog logEntry = new EmailLog();

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(request.getTo());
            message.setSubject(request.getSubject());
            message.setText(request.getContent());

            mailSender.send(message);

            response.setMessageId(UUID.randomUUID().toString());
            response.setTo(request.getTo());
            response.setSubject(request.getSubject());
            response.setSentAt(LocalDateTime.now());
            response.setSuccess(true);

            logEntry.setRecipient(request.getTo());
            logEntry.setSubject(request.getSubject());
            logEntry.setContent(request.getContent());
            logEntry.setEmailType("SIMPLE");
            logEntry.setStatus("SENT");
            logEntry.setSentAt(LocalDateTime.now());

            log.info("Email simple envoyé à {}", request.getTo());

        } catch (Exception e) {
            log.error("Erreur envoi email simple: {}", e.getMessage());
            response.setSuccess(false);
            response.setErrorMessage(e.getMessage());

            logEntry.setRecipient(request.getTo());
            logEntry.setSubject(request.getSubject());
            logEntry.setContent(request.getContent());
            logEntry.setEmailType("SIMPLE");
            logEntry.setStatus("FAILED");
            logEntry.setErrorMessage(e.getMessage());
        }

        emailLogRepository.save(logEntry);
        return response;
    }

    @Override
    public EmailResponse sendHtmlEmail(EmailRequest request) {
        EmailResponse response = new EmailResponse();
        EmailLog logEntry = new EmailLog();

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(request.getTo());
            helper.setSubject(request.getSubject());
            helper.setText(request.getContent(), true);

            mailSender.send(message);

            response.setMessageId(UUID.randomUUID().toString());
            response.setTo(request.getTo());
            response.setSubject(request.getSubject());
            response.setSentAt(LocalDateTime.now());
            response.setSuccess(true);

            logEntry.setRecipient(request.getTo());
            logEntry.setSubject(request.getSubject());
            logEntry.setContent(request.getContent());
            logEntry.setEmailType("HTML");
            logEntry.setStatus("SENT");
            logEntry.setSentAt(LocalDateTime.now());

            log.info("Email HTML envoyé à {}", request.getTo());

        } catch (Exception e) {
            log.error("Erreur envoi email HTML: {}", e.getMessage());
            response.setSuccess(false);
            response.setErrorMessage(e.getMessage());

            logEntry.setRecipient(request.getTo());
            logEntry.setSubject(request.getSubject());
            logEntry.setContent(request.getContent());
            logEntry.setEmailType("HTML");
            logEntry.setStatus("FAILED");
            logEntry.setErrorMessage(e.getMessage());
        }

        emailLogRepository.save(logEntry);
        return response;
    }

    @Override
    public EmailResponse sendReservationConfirmation(Reservation reservation) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("firstName", reservation.getUser().getFirstName());
        variables.put("reservationNumber", reservation.getReservationNumber());
        variables.put("pickupDate", reservation.getPickupDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        variables.put("returnDate", reservation.getReturnDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        variables.put("totalAmount", String.format("%.2f €", reservation.getTotalAmount()));
        variables.put("items", reservation.getItems());
        variables.put("reservationUrl", baseUrl + "/reservations/" + reservation.getId());
        variables.put("currentYear", LocalDateTime.now().getYear());

        return sendTemplateEmail(
                reservation.getUser().getEmail(),
                "Confirmation de votre réservation #" + reservation.getReservationNumber(),
                "reservation-confirmation",
                variables
        );
    }

    @Override
    public EmailResponse sendReservationPending(Reservation reservation) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("firstName", reservation.getUser().getFirstName());
        variables.put("reservationNumber", reservation.getReservationNumber());
        variables.put("pickupDate", reservation.getPickupDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        variables.put("returnDate", reservation.getReturnDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        variables.put("totalAmount", String.format("%.2f €", reservation.getTotalAmount()));
        variables.put("items", reservation.getItems());
        variables.put("currentYear", LocalDateTime.now().getYear());

        return sendTemplateEmail(
                reservation.getUser().getEmail(),
                "Votre réservation #" + reservation.getReservationNumber() + " est en attente",
                "reservation-pending",
                variables
        );
    }

    @Override
    public EmailResponse sendPaymentConfirmation(Reservation reservation) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("firstName", reservation.getUser().getFirstName());
        variables.put("reservationNumber", reservation.getReservationNumber());
        variables.put("totalAmount", String.format("%.2f €", reservation.getTotalAmount()));
        variables.put("currentYear", LocalDateTime.now().getYear());

        return sendTemplateEmail(
                reservation.getUser().getEmail(),
                "Confirmation de paiement - Réservation #" + reservation.getReservationNumber(),
                "payment-confirmation",
                variables
        );
    }

    @Override
    public EmailResponse sendCancellationConfirmation(Reservation reservation) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("firstName", reservation.getUser().getFirstName());
        variables.put("reservationNumber", reservation.getReservationNumber());
        variables.put("currentYear", LocalDateTime.now().getYear());

        return sendTemplateEmail(
                reservation.getUser().getEmail(),
                "Confirmation d'annulation - Réservation #" + reservation.getReservationNumber(),
                "cancellation-confirmation",
                variables
        );
    }
    @Override
    public EmailResponse sendPasswordResetEmail(User user, String resetToken) {
        String resetUrl = baseUrl + "/reset-password?token=" + resetToken;

        Map<String, Object> variables = new HashMap<>();
        variables.put("firstName", user.getFirstName());
        variables.put("resetUrl", resetUrl);
        variables.put("expirationHours", 24);
        variables.put("currentYear", LocalDateTime.now().getYear());

        return sendTemplateEmail(
                user.getEmail(),
                "Réinitialisation de votre mot de passe",
                "password-reset",
                variables
        );
    }

    @Override
    public EmailResponse sendPickupReminder(Reservation reservation) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("firstName", reservation.getUser().getFirstName());
        variables.put("reservationNumber", reservation.getReservationNumber());
        variables.put("pickupDate", reservation.getPickupDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        variables.put("items", reservation.getItems());
        variables.put("currentYear", LocalDateTime.now().getYear());

        return sendTemplateEmail(
                reservation.getUser().getEmail(),
                "Rappel - Retrait de votre matériel demain",
                "pickup-reminder",
                variables
        );
    }

    @Override
    public EmailResponse sendWelcomeEmail(User user) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("firstName", user.getFirstName());
        variables.put("loginUrl", baseUrl + "/login");
        variables.put("currentYear", LocalDateTime.now().getYear());

        return sendTemplateEmail(
                user.getEmail(),
                "Bienvenue sur Location Événement",
                "welcome-email",
                variables
        );
    }

    private EmailResponse sendTemplateEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        EmailRequest request = new EmailRequest();
        request.setTo(to);
        request.setSubject(subject);
        request.setHtml(true);

        try {
            Context context = new Context();
            context.setVariables(variables);
            String htmlContent = templateEngine.process("email/" + templateName, context);
            request.setContent(htmlContent);
        } catch (Exception e) {
            log.error("Erreur génération template {}: {}", templateName, e.getMessage());
            EmailResponse errorResponse = new EmailResponse();
            errorResponse.setSuccess(false);
            errorResponse.setErrorMessage("Erreur de template: " + e.getMessage());
            return errorResponse;
        }

        return sendHtmlEmail(request);
    }
}