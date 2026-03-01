package com.location.evenement.service.impl;

import com.location.evenement.dto.request.ReservationRequest;
import com.location.evenement.dto.response.ReservationResponse;
import com.location.evenement.dto.mapper.ReservationMapper;
import com.location.evenement.exception.BadRequestException;
import com.location.evenement.exception.ResourceNotFoundException;
import com.location.evenement.model.Product;
import com.location.evenement.model.Reservation;
import com.location.evenement.model.ReservationItem;
import com.location.evenement.model.User;
import com.location.evenement.model.enums.PaymentStatus;
import com.location.evenement.model.enums.ReservationStatus;
import com.location.evenement.repository.ProductRepository;
import com.location.evenement.repository.ReservationRepository;
import com.location.evenement.repository.UserRepository;
import com.location.evenement.service.CartService;
import com.location.evenement.service.EmailService;
import com.location.evenement.service.ProductService;
import com.location.evenement.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final CartService cartService;
    private final EmailService emailService;
    private final ReservationMapper reservationMapper;

    @Override
    @Transactional
    public ReservationResponse createReservation(Long userId, ReservationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BadRequestException("La réservation doit contenir au moins un article");
        }

        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setPickupDate(request.getPickupDate());
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setPaymentStatus(PaymentStatus.PENDING);

        BigDecimal total = BigDecimal.ZERO;
        for (ReservationRequest.Item itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé avec l'id: " + itemRequest.getProductId()));

            if (!productService.checkStockAvailability(product.getId(), itemRequest.getQuantity())) {
                throw new BadRequestException("Stock insuffisant pour le produit: " + product.getName());
            }

            ReservationItem item = new ReservationItem();
            item.setReservation(reservation);
            item.setProduct(product);
            item.setQuantity(itemRequest.getQuantity());
            item.setDurationDays(itemRequest.getDurationDays());
            item.setPricePerDay(product.getPricePerDay());

            BigDecimal subtotal = product.getPricePerDay()
                    .multiply(BigDecimal.valueOf(itemRequest.getQuantity()))
                    .multiply(BigDecimal.valueOf(itemRequest.getDurationDays()));
            item.setSubtotal(subtotal);

            reservation.getItems().add(item);
            total = total.add(subtotal);
        }

        int maxDuration = request.getItems().stream()
                .mapToInt(ReservationRequest.Item::getDurationDays)
                .max().orElse(0);
        reservation.setReturnDate(request.getPickupDate().plusDays(maxDuration));
        reservation.setTotalAmount(total);

        Reservation savedReservation = reservationRepository.save(reservation);

        cartService.clearCart(userId);
        emailService.sendReservationPending(savedReservation);

        return reservationMapper.toResponse(savedReservation);
    }

    @Override
    public ReservationResponse getReservationById(Long id) {
        return reservationMapper.toResponse(findReservationById(id));
    }

    @Override
    public ReservationResponse getReservationByNumber(String reservationNumber) {
        Reservation reservation = reservationRepository.findByReservationNumber(reservationNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation non trouvée avec le numéro: " + reservationNumber));
        return reservationMapper.toResponse(reservation);
    }

    @Override
    public List<ReservationResponse> getUserReservations(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
        return reservationRepository.findByUser(user).stream()
                .map(reservationMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReservationResponse> getAllReservations() {
        return reservationRepository.findAll().stream()
                .map(reservationMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReservationResponse> getReservationsByStatus(ReservationStatus status) {
        return reservationRepository.findByStatus(status).stream()
                .map(reservationMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReservationResponse> getReservationsBetweenDates(LocalDate start, LocalDate end) {
        return reservationRepository.findReservationsBetweenDates(start, end).stream()
                .map(reservationMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ReservationResponse updateReservationStatus(Long id, ReservationStatus newStatus) {
        Reservation reservation = findReservationById(id);

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new BadRequestException("Impossible de modifier une réservation annulée");
        }

        ReservationStatus oldStatus = reservation.getStatus();
        reservation.setStatus(newStatus);

        if (newStatus == ReservationStatus.CANCELLED) {
            reservation.setPaymentStatus(PaymentStatus.CANCELLED);
        }

        Reservation updatedReservation = reservationRepository.save(reservation);

        if (newStatus == ReservationStatus.CONFIRMED && oldStatus == ReservationStatus.PENDING) {
            emailService.sendReservationConfirmation(updatedReservation);
        }

        return reservationMapper.toResponse(updatedReservation);
    }

    @Override
    @Transactional
    public ReservationResponse cancelReservation(Long id) {
        return updateReservationStatus(id, ReservationStatus.CANCELLED);
    }

    @Override
    @Transactional
    public ReservationResponse confirmPickupAndPayment(Long id) {
        Reservation reservation = findReservationById(id);

        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new BadRequestException("La réservation doit être confirmée");
        }

        if (reservation.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new BadRequestException("Le paiement n'est pas en attente");
        }

        for (ReservationItem item : reservation.getItems()) {
            Product product = item.getProduct();
            int newStock = product.getStockQuantity() - item.getQuantity();

            if (newStock < 0) {
                throw new BadRequestException("Stock insuffisant pour le produit: " + product.getName());
            }

            product.setStockQuantity(newStock);
            product.setIsAvailable(newStock > 0);
            productRepository.save(product);
        }

        reservation.setStatus(ReservationStatus.IN_PROGRESS);
        reservation.setPaymentStatus(PaymentStatus.PAID_AT_PICKUP);

        Reservation updatedReservation = reservationRepository.save(reservation);
        emailService.sendPaymentConfirmation(updatedReservation);

        return reservationMapper.toResponse(updatedReservation);
    }

    @Override
    public List<ReservationResponse> getReservationsPendingPickup(LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now();

        return reservationRepository.findAll().stream()
                .filter(r -> r.getPickupDate().equals(targetDate) &&
                        r.getStatus() == ReservationStatus.CONFIRMED &&
                        r.getPaymentStatus() == PaymentStatus.PENDING)
                .map(reservationMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ReservationResponse cancelWithoutPenalty(Long id) {
        Reservation reservation = findReservationById(id);

        if (reservation.getPickupDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Impossible d'annuler une réservation passée");
        }

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new BadRequestException("La réservation est déjà annulée");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservation.setPaymentStatus(PaymentStatus.CANCELLED);

        Reservation updatedReservation = reservationRepository.save(reservation);
        emailService.sendCancellationConfirmation(updatedReservation);

        return reservationMapper.toResponse(updatedReservation);
    }

    @Override
    @Transactional
    public void deleteReservation(Long id) {
        Reservation reservation = findReservationById(id);
        reservationRepository.delete(reservation);
    }

    private Reservation findReservationById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation non trouvée avec l'id: " + id));
    }
}