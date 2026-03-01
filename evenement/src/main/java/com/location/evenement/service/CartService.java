package com.location.evenement.service;

import com.location.evenement.dto.request.CartItemRequest;
import com.location.evenement.dto.response.CartResponse;

public interface CartService {
    CartResponse getCartByUser(Long userId);
    CartResponse addItemToCart(Long userId, CartItemRequest request);
    CartResponse updateCartItem(Long userId, Long itemId, CartItemRequest request);
    CartResponse removeItemFromCart(Long userId, Long itemId);
    void clearCart(Long userId);
    CartResponse validateCart(Long userId);
}