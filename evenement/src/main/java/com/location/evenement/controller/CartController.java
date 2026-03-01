package com.location.evenement.controller;

import com.location.evenement.dto.request.CartItemRequest;
import com.location.evenement.dto.response.CartResponse;
import com.location.evenement.model.User;
import com.location.evenement.service.CartService;
import com.location.evenement.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<CartResponse> getCart() {
        User currentUser = userService.getCurrentUser();
        return ResponseEntity.ok(cartService.getCartByUser(currentUser.getId()));
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(@Valid @RequestBody CartItemRequest request) {
        User currentUser = userService.getCurrentUser();
        return ResponseEntity.ok(cartService.addItemToCart(currentUser.getId(), request));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> updateItem(@PathVariable Long itemId, @Valid @RequestBody CartItemRequest request) {
        User currentUser = userService.getCurrentUser();
        return ResponseEntity.ok(cartService.updateCartItem(currentUser.getId(), itemId, request));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> removeItem(@PathVariable Long itemId) {
        User currentUser = userService.getCurrentUser();
        return ResponseEntity.ok(cartService.removeItemFromCart(currentUser.getId(), itemId));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart() {
        User currentUser = userService.getCurrentUser();
        cartService.clearCart(currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/validate")
    public ResponseEntity<CartResponse> validateCart() {
        User currentUser = userService.getCurrentUser();
        return ResponseEntity.ok(cartService.validateCart(currentUser.getId()));
    }
}