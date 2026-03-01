package com.location.evenement.service.impl;

import com.location.evenement.dto.request.CartItemRequest;
import com.location.evenement.dto.response.CartResponse;
import com.location.evenement.dto.mapper.CartMapper;
import com.location.evenement.exception.BadRequestException;
import com.location.evenement.exception.ResourceNotFoundException;
import com.location.evenement.model.Cart;
import com.location.evenement.model.CartItem;
import com.location.evenement.model.Product;
import com.location.evenement.model.User;
import com.location.evenement.repository.CartRepository;
import com.location.evenement.repository.ProductRepository;
import com.location.evenement.repository.UserRepository;
import com.location.evenement.service.CartService;
import com.location.evenement.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final CartMapper cartMapper;

    @Override
    public CartResponse getCartByUser(Long userId) {
        return cartMapper.toResponse(getOrCreateCart(userId));
    }

    @Override
    @Transactional
    public CartResponse addItemToCart(Long userId, CartItemRequest request) {
        Cart cart = getOrCreateCart(userId);

        if (!productService.checkStockAvailability(request.getProductId(), request.getQuantity())) {
            throw new BadRequestException("Produit non disponible en quantité suffisante");
        }

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé"));

        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(request.getProductId()))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
            existingItem.setDurationDays(request.getDurationDays());
            existingItem.setPickupDate(request.getPickupDate() != null ? request.getPickupDate() : LocalDate.now());
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(request.getQuantity());
            newItem.setDurationDays(request.getDurationDays());
            newItem.setPickupDate(request.getPickupDate() != null ? request.getPickupDate() : LocalDate.now());
            cart.getItems().add(newItem);
        }

        return cartMapper.toResponse(cartRepository.save(cart));
    }

    @Override
    @Transactional
    public CartResponse updateCartItem(Long userId, Long itemId, CartItemRequest request) {
        Cart cart = getOrCreateCart(userId);

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Article non trouvé dans le panier"));

        if (!productService.checkStockAvailability(request.getProductId(), request.getQuantity())) {
            throw new BadRequestException("Produit non disponible en quantité suffisante");
        }

        item.setQuantity(request.getQuantity());
        item.setDurationDays(request.getDurationDays());
        if (request.getPickupDate() != null) {
            item.setPickupDate(request.getPickupDate());
        }

        return cartMapper.toResponse(cartRepository.save(cart));
    }

    @Override
    @Transactional
    public CartResponse removeItemFromCart(Long userId, Long itemId) {
        Cart cart = getOrCreateCart(userId);

        boolean removed = cart.getItems().removeIf(item -> item.getId().equals(itemId));
        if (!removed) {
            throw new ResourceNotFoundException("Article non trouvé dans le panier");
        }

        return cartMapper.toResponse(cartRepository.save(cart));
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    @Override
    public CartResponse validateCart(Long userId) {
        Cart cart = getOrCreateCart(userId);

        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Le panier est vide");
        }

        for (CartItem item : cart.getItems()) {
            if (!productService.checkStockAvailability(item.getProduct().getId(), item.getQuantity())) {
                throw new BadRequestException("Le produit " + item.getProduct().getName() +
                        " n'est plus disponible en quantité suffisante");
            }
        }

        return cartMapper.toResponse(cart);
    }

    private Cart getOrCreateCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });
    }
}