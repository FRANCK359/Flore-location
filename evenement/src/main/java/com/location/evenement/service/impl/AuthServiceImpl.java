package com.location.evenement.service.impl;

import com.location.evenement.dto.request.ForgotPasswordRequest;
import com.location.evenement.dto.request.LoginRequest;
import com.location.evenement.dto.request.RegisterRequest;
import com.location.evenement.dto.request.ResetPasswordRequest;
import com.location.evenement.dto.response.JwtResponse;
import com.location.evenement.dto.response.MessageResponse;
import com.location.evenement.dto.response.UserResponse;
import com.location.evenement.dto.mapper.UserMapper;
import com.location.evenement.exception.BadRequestException;
import com.location.evenement.exception.UnauthorizedException;
import com.location.evenement.model.PasswordResetToken;
import com.location.evenement.model.User;
import com.location.evenement.repository.PasswordResetTokenRepository;
import com.location.evenement.repository.UserRepository;
import com.location.evenement.service.AuthService;
import com.location.evenement.service.EmailService;
import com.location.evenement.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;
    private final EmailService emailService;

    @Override
    public JwtResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtil.generateToken(authentication);

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new UnauthorizedException("Utilisateur non trouvé"));

            return new JwtResponse(jwt, user.getId(), user.getEmail(),
                    user.getFirstName(), user.getLastName());
        } catch (Exception e) {
            throw new UnauthorizedException("Email ou mot de passe incorrect");
        }
    }

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Les mots de passe ne correspondent pas");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Cet email est déjà utilisé");
        }

        User user = userMapper.toEntity(request);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(user);
        emailService.sendWelcomeEmail(savedUser);

        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);

        // Ne pas révéler si l'email existe (sécurité)
        if (user == null) {
            log.info("Tentative de réinitialisation pour email inexistant: {}", request.getEmail());
            return new MessageResponse("Si votre email existe dans notre système, vous recevrez un lien de réinitialisation");
        }

        // Supprimer les anciens tokens
        tokenRepository.findByUser(user).ifPresent(tokenRepository::delete);

        // Créer nouveau token
        PasswordResetToken resetToken = new PasswordResetToken(user);
        tokenRepository.save(resetToken);

        // Envoyer email
        emailService.sendPasswordResetEmail(user, resetToken.getToken());

        log.info("Email de réinitialisation envoyé à: {}", user.getEmail());
        return new MessageResponse("Si votre email existe dans notre système, vous recevrez un lien de réinitialisation");
    }

    @Override
    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        // Vérifier que les mots de passe correspondent
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Les mots de passe ne correspondent pas");
        }

        // Validation basique du mot de passe
        if (request.getNewPassword().length() < 6) {
            throw new BadRequestException("Le mot de passe doit contenir au moins 6 caractères");
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

        // Supprimer le token (utilisé)
        tokenRepository.delete(resetToken);

        log.info("Mot de passe réinitialisé avec succès pour: {}", user.getEmail());
        return new MessageResponse("Votre mot de passe a été réinitialisé avec succès");
    }
}