package com.location.evenement.dto.response;

import com.location.evenement.model.enums.PaymentStatus;
import com.location.evenement.model.enums.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponse {
    private Long id;
    private String reservationNumber;
    private LocalDate pickupDate;
    private LocalDate returnDate;
    private BigDecimal totalAmount;
    private ReservationStatus status;
    private PaymentStatus paymentStatus;
    private LocalDateTime createdAt;
    private List<ReservationItemResponse> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReservationItemResponse {
        private Long productId;
        private String productName;
        private Integer quantity;
        private Integer durationDays;
        private BigDecimal pricePerDay;
        private BigDecimal subtotal;
    }
}