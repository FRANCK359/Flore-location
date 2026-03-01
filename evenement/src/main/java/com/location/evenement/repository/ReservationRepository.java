package com.location.evenement.repository;

import com.location.evenement.model.Reservation;
import com.location.evenement.model.User;
import com.location.evenement.model.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUser(User user);
    List<Reservation> findByStatus(ReservationStatus status);
    Optional<Reservation> findByReservationNumber(String reservationNumber);

    @Query("SELECT r FROM Reservation r WHERE r.pickupDate BETWEEN :start AND :end")
    List<Reservation> findReservationsBetweenDates(@Param("start") LocalDate start, @Param("end") LocalDate end);
}