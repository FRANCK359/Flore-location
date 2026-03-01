package com.location.evenement.service.impl;

import com.location.evenement.dto.request.ForgotPasswordRequest;
import com.location.evenement.dto.request.ResetPasswordRequest;
import com.location.evenement.dto.response.MessageResponse;
import com.location.evenement.exception.BadRequestException;
import com.location.evenement.model.PasswordResetToken;
import com.location.evenement.model.User;
import com.location.evenement.repository.PasswordResetTokenRepository;
import com.location.evenement.repository.UserRepository;
import com.location.evenement.service.EmailService;
import com.location.evenement.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);

        // Ne pas révéler si l'email existe ou pas (sécurité)
        if (user == null) {
            log.info("Tentative de réinitialisation pour email inexistant: {}", request.getEmail());
            return new MessageResponse("Si cet email existe, vous recevrez un lien de réinitialisation");
        }

        // Supprimer les anciens tokens
        tokenRepository.findByUser(user).ifPresent(tokenRepository::delete);

        // Créer nouveau token
        PasswordResetToken resetToken = new PasswordResetToken(user);
        tokenRepository.save(resetToken);

        // Envoyer email
        emailService.sendPasswordResetEmail(user, resetToken.getToken());

        log.info("Email de réinitialisation envoyé à: {}", user.getEmail());
        return new MessageResponse("Si cet email existe, vous recevrez un lien de réinitialisation");
    }

    @Override
    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        // Vérifier que les mots de passe correspondent
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Les mots de passe ne correspondent pas");
        }

        // Récupérer le token
        PasswordResetToken resetToken = tokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new BadRequestException("Token invalide ou expiré"));

        // Vérifier si le token est valide
        if (!resetToken.isValid()) {
            tokenRepository.delete(resetToken);
            throw new BadRequestException("Token expiré ou déjà utilisé");
        }

        // Récupérer l'utilisateur
        User user = resetToken.getUser();

        // Mettre à jour le mot de passe
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Marquer le token comme utilisé et le supprimer
        resetToken.setUsed(true);
        tokenRepository.delete(resetToken);

        log.info("Mot de passe réinitialisé pour: {}", user.getEmail());
        return new MessageResponse("Votre mot de passe a été réinitialisé avec succès");
    }
}