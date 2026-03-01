package com.location.evenement.service;

import com.location.evenement.dto.request.EmailRequest;
import com.location.evenement.dto.response.EmailResponse;
import com.location.evenement.model.Reservation;
import com.location.evenement.model.User;

public interface EmailService {
    // Méthodes de base
    EmailResponse sendSimpleEmail(EmailRequest request);
    EmailResponse sendHtmlEmail(EmailRequest request);

    // Méthodes spécifiques aux réservations
    EmailResponse sendReservationConfirmation(Reservation reservation);
    EmailResponse sendReservationPending(Reservation reservation);
    EmailResponse sendPaymentConfirmation(Reservation reservation);
    EmailResponse sendCancellationConfirmation(Reservation reservation);
    EmailResponse sendPickupReminder(Reservation reservation);

    // Méthodes utilisateur
    EmailResponse sendWelcomeEmail(User user);
    EmailResponse sendPasswordResetEmail(User user, String resetToken);
}