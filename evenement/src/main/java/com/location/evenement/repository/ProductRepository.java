package com.location.evenement.repository;

import com.location.evenement.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategoryId(Long categoryId);
    List<Product> findByIsAvailableTrue();
    Optional<Product> findByReference(String reference);
    boolean existsByReference(String reference);

    @Query("SELECT COUNT(p) > 0 FROM Product p WHERE p.reference = :ref AND p.id != :id")
    boolean existsByReferenceAndIdNot(@Param("ref") String reference, @Param("id") Long id);
}