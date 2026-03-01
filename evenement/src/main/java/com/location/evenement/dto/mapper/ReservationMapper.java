package com.location.evenement.dto.mapper;

import com.location.evenement.dto.response.ReservationResponse;
import com.location.evenement.model.Reservation;
import com.location.evenement.model.ReservationItem;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReservationMapper {

    public ReservationResponse toResponse(Reservation reservation) {
        ReservationResponse response = new ReservationResponse();
        response.setId(reservation.getId());
        response.setReservationNumber(reservation.getReservationNumber());
        response.setPickupDate(reservation.getPickupDate());
        response.setReturnDate(reservation.getReturnDate());
        response.setTotalAmount(reservation.getTotalAmount());
        response.setStatus(reservation.getStatus());
        response.setPaymentStatus(reservation.getPaymentStatus());
        response.setCreatedAt(reservation.getCreatedAt());
        response.setItems(mapItems(reservation.getItems()));
        return response;
    }

    private List<ReservationResponse.ReservationItemResponse> mapItems(List<ReservationItem> items) {
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }
        return items.stream().map(this::toItemResponse).collect(Collectors.toList());
    }

    private ReservationResponse.ReservationItemResponse toItemResponse(ReservationItem item) {
        ReservationResponse.ReservationItemResponse response = new ReservationResponse.ReservationItemResponse();
        response.setProductId(item.getProduct().getId());
        response.setProductName(item.getProduct().getName());
        response.setQuantity(item.getQuantity());
        response.setDurationDays(item.getDurationDays());
        response.setPricePerDay(item.getPricePerDay());
        response.setSubtotal(item.getSubtotal());
        return response;
    }
}