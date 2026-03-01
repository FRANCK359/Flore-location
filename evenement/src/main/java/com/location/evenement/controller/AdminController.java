package com.location.evenement.controller;

import com.location.evenement.dto.response.UserResponse;
import com.location.evenement.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Administration", description = "Endpoints réservés aux administrateurs")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final UserService userService;

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Récupérer tous les utilisateurs", description = "Retourne la liste de tous les utilisateurs")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping("/users/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Changer le rôle d'un utilisateur")
    public ResponseEntity<UserResponse> changeUserRole(@PathVariable Long userId, @RequestParam String role) {
        // Implémenter la logique de changement de rôle
        return ResponseEntity.ok().build();
    }

    @PutMapping("/users/{userId}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activer/désactiver un utilisateur")
    public ResponseEntity<UserResponse> toggleUserStatus(@PathVariable Long userId) {
        // Implémenter la logique d'activation/désactivation
        return ResponseEntity.ok().build();
    }
}