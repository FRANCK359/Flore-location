package com.location.evenement.config;

import com.location.evenement.model.User;
import com.location.evenement.model.enums.Role;
import com.location.evenement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Créer l'admin s'il n'existe pas
        if (!userRepository.existsByEmail("admin@location.com")) {
            User admin = new User();
            admin.setEmail("admin@location.com");
            admin.setPasswordHash(passwordEncoder.encode("admin123"));
            admin.setFirstName("Admin");
            admin.setLastName("System");
            admin.setPhone("0123456789");
            admin.setAddress("Paris");
            admin.setIsActive(true);
            admin.setRole(Role.ADMIN);

            userRepository.save(admin);
            log.info("✅ Admin créé avec succès - email: admin@location.com / mot de passe: admin123");
        } else {
            log.info("✅ L'admin existe déjà");
        }

        // Créer un utilisateur test s'il n'existe pas
        if (!userRepository.existsByEmail("user@test.com")) {
            User user = new User();
            user.setEmail("user@test.com");
            user.setPasswordHash(passwordEncoder.encode("user123"));
            user.setFirstName("Test");
            user.setLastName("User");
            user.setPhone("0123456789");
            user.setAddress("Paris");
            user.setIsActive(true);
            user.setRole(Role.USER);

            userRepository.save(user);
            log.info("✅ Utilisateur test créé - email: user@test.com / mot de passe: user123");
        }
    }
}