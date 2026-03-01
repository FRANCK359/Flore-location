package com.location.evenement.repository;

import com.location.evenement.model.Cart;
import com.location.evenement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUser(User user);
}