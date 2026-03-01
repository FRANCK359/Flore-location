package com.location.evenement.dto.mapper;

import com.location.evenement.dto.response.CartResponse;
import com.location.evenement.model.Cart;
import com.location.evenement.model.CartItem;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.stream.Collectors;

@Component
public class CartMapper {

    public CartResponse toResponse(Cart cart) {
        CartResponse response = new CartResponse();
        response.setId(cart.getId());

        if (cart.getItems() != null && !cart.getItems().isEmpty()) {
            response.setItems(cart.getItems().stream()
                    .map(this::toItemResponse)
                    .collect(Collectors.toList()));

            BigDecimal total = cart.getItems().stream()
                    .map(this::calculateSubtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            response.setTotal(total);
        }

        return response;
    }

    private CartResponse.CartItemResponse toItemResponse(CartItem item) {
        CartResponse.CartItemResponse response = new CartResponse.CartItemResponse();
        response.setId(item.getId());
        response.setProductId(item.getProduct().getId());
        response.setProductName(item.getProduct().getName());

        String productImage = item.getProduct().getMainImage();
        if (productImage == null && item.getProduct().getImages() != null && !item.getProduct().getImages().isEmpty()) {
            productImage = item.getProduct().getImages().get(0);
        }
        response.setProductImage(productImage);

        response.setQuantity(item.getQuantity());
        response.setDurationDays(item.getDurationDays());
        response.setPricePerDay(item.getProduct().getPricePerDay());
        response.setPickupDate(item.getPickupDate());
        response.setSubtotal(calculateSubtotal(item));
        return response;
    }

    private BigDecimal calculateSubtotal(CartItem item) {
        return item.getProduct().getPricePerDay()
                .multiply(BigDecimal.valueOf(item.getQuantity()))
                .multiply(BigDecimal.valueOf(item.getDurationDays()));
    }
}