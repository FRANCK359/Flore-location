package com.location.evenement.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class ReservationRequest {
    @NotNull(message = "La date de retrait est obligatoire")
    @Future(message = "La date de retrait doit être dans le futur")
    private LocalDate pickupDate;

    @NotEmpty(message = "La réservation doit contenir au moins un article")
    @Valid
    private List<Item> items;

    @Data
    public static class Item {
        @NotNull(message = "L'ID du produit est obligatoire")
        private Long productId;

        @NotNull(message = "La quantité est obligatoire")
        private Integer quantity;

        @NotNull(message = "La durée est obligatoire")
        private Integer durationDays;
    }
}