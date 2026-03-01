package com.location.evenement.service;

import com.location.evenement.dto.request.ReservationRequest;
import com.location.evenement.dto.response.ReservationResponse;
import com.location.evenement.model.enums.ReservationStatus;
import java.time.LocalDate;
import java.util.List;

public interface ReservationService {
    ReservationResponse createReservation(Long userId, ReservationRequest request);
    ReservationResponse getReservationById(Long id);
    ReservationResponse getReservationByNumber(String reservationNumber);
    List<ReservationResponse> getUserReservations(Long userId);
    List<ReservationResponse> getAllReservations();
    List<ReservationResponse> getReservationsByStatus(ReservationStatus status);
    List<ReservationResponse> getReservationsBetweenDates(LocalDate start, LocalDate end);
    ReservationResponse updateReservationStatus(Long id, ReservationStatus status);
    ReservationResponse cancelReservation(Long id);
    ReservationResponse confirmPickupAndPayment(Long id);
    List<ReservationResponse> getReservationsPendingPickup(LocalDate date);
    ReservationResponse cancelWithoutPenalty(Long id);
    void deleteReservation(Long id);
}