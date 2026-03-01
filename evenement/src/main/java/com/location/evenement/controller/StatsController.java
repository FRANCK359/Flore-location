package com.location.evenement.controller;

import com.location.evenement.dto.response.CategoryResponse;
import com.location.evenement.dto.response.PaymentStatsResponse;
import com.location.evenement.dto.response.ProductResponse;
import com.location.evenement.dto.response.ReservationResponse;
import com.location.evenement.model.enums.PaymentStatus;
import com.location.evenement.model.enums.ReservationStatus;
import com.location.evenement.service.CategoryService;
import com.location.evenement.service.ProductService;
import com.location.evenement.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final ReservationService reservationService;
    private final ProductService productService;
    private final CategoryService categoryService;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        List<ReservationResponse> allReservations = reservationService.getAllReservations();
        YearMonth currentMonth = YearMonth.now();

        Map<String, Object> stats = new HashMap<>();

        // Produits
        stats.put("totalProducts", productService.getAllProducts().size());
        stats.put("availableProducts", productService.getAvailableProducts().size());

        // Catégories
        stats.put("totalCategories", categoryService.getAllCategories().size());

        // Réservations
        stats.put("totalReservations", allReservations.size());
        stats.put("pendingReservations", countByStatus(allReservations, ReservationStatus.PENDING));
        stats.put("confirmedReservations", countByStatus(allReservations, ReservationStatus.CONFIRMED));
        stats.put("inProgressReservations", countByStatus(allReservations, ReservationStatus.IN_PROGRESS));
        stats.put("completedReservations", countByStatus(allReservations, ReservationStatus.COMPLETED));
        stats.put("cancelledReservations", countByStatus(allReservations, ReservationStatus.CANCELLED));

        // Revenus
        stats.put("totalRevenue", calculateRevenue(allReservations));

        // Statistiques du mois
        List<ReservationResponse> monthReservations = reservationService.getReservationsBetweenDates(
                currentMonth.atDay(1),
                currentMonth.atEndOfMonth()
        );
        stats.put("monthReservations", monthReservations.size());
        stats.put("monthRevenue", calculateRevenue(monthReservations));

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/revenue/monthly")
    public ResponseEntity<Map<String, BigDecimal>> getMonthlyRevenue() {
        Map<String, BigDecimal> monthlyRevenue = new HashMap<>();
        YearMonth currentMonth = YearMonth.now();

        for (int i = 0; i < 6; i++) {
            YearMonth month = currentMonth.minusMonths(i);
            List<ReservationResponse> reservations = reservationService.getReservationsBetweenDates(
                    month.atDay(1),
                    month.atEndOfMonth()
            );

            monthlyRevenue.put(month.getMonth() + " " + month.getYear(), calculateRevenue(reservations));
        }

        return ResponseEntity.ok(monthlyRevenue);
    }

    @GetMapping("/payments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentStatsResponse> getPaymentStats() {
        List<ReservationResponse> allReservations = reservationService.getAllReservations();

        long pendingCount = 0;
        long paidCount = 0;
        long cancelledCount = 0;
        BigDecimal pendingAmount = BigDecimal.ZERO;
        BigDecimal collectedAmount = BigDecimal.ZERO;

        for (ReservationResponse reservation : allReservations) {
            switch (reservation.getPaymentStatus()) {
                case PENDING:
                    pendingCount++;
                    pendingAmount = pendingAmount.add(reservation.getTotalAmount());
                    break;
                case PAID_AT_PICKUP:
                    paidCount++;
                    collectedAmount = collectedAmount.add(reservation.getTotalAmount());
                    break;
                case CANCELLED:
                    cancelledCount++;
                    break;
            }
        }

        return ResponseEntity.ok(new PaymentStatsResponse(
                pendingCount, paidCount, cancelledCount, pendingAmount, collectedAmount
        ));
    }

    private long countByStatus(List<ReservationResponse> reservations, ReservationStatus status) {
        return reservations.stream().filter(r -> r.getStatus() == status).count();
    }

    private BigDecimal calculateRevenue(List<ReservationResponse> reservations) {
        return reservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.COMPLETED || r.getStatus() == ReservationStatus.IN_PROGRESS)
                .map(ReservationResponse::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}