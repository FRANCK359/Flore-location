package com.location.evenement.controller;

import com.location.evenement.dto.request.ReservationRequest;
import com.location.evenement.dto.response.ReservationResponse;
import com.location.evenement.model.User;
import com.location.evenement.model.enums.ReservationStatus;
import com.location.evenement.service.ReservationService;
import com.location.evenement.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(@Valid @RequestBody ReservationRequest request) {
        User currentUser = userService.getCurrentUser();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservationService.createReservation(currentUser.getId(), request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponse> getReservationById(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.getReservationById(id));
    }

    @GetMapping("/number/{reservationNumber}")
    public ResponseEntity<ReservationResponse> getReservationByNumber(@PathVariable String reservationNumber) {
        return ResponseEntity.ok(reservationService.getReservationByNumber(reservationNumber));
    }

    @GetMapping("/user")
    public ResponseEntity<List<ReservationResponse>> getMyReservations() {
        User currentUser = userService.getCurrentUser();
        return ResponseEntity.ok(reservationService.getUserReservations(currentUser.getId()));
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> getAllReservations() {
        return ResponseEntity.ok(reservationService.getAllReservations());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ReservationResponse>> getReservationsByStatus(@PathVariable ReservationStatus status) {
        return ResponseEntity.ok(reservationService.getReservationsByStatus(status));
    }

    @GetMapping("/between-dates")
    public ResponseEntity<List<ReservationResponse>> getReservationsBetweenDates(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(reservationService.getReservationsBetweenDates(start, end));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ReservationResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam ReservationStatus status) {
        return ResponseEntity.ok(reservationService.updateReservationStatus(id, status));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ReservationResponse> cancelReservation(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.cancelReservation(id));
    }

    @PostMapping("/{id}/confirm-pickup")
    public ResponseEntity<ReservationResponse> confirmPickup(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.confirmPickupAndPayment(id));
    }

    @GetMapping("/pending-pickup")
    public ResponseEntity<List<ReservationResponse>> getPendingPickup(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(reservationService.getReservationsPendingPickup(date));
    }

    @PostMapping("/{id}/cancel-without-penalty")
    public ResponseEntity<ReservationResponse> cancelWithoutPenalty(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.cancelWithoutPenalty(id));
    }
}