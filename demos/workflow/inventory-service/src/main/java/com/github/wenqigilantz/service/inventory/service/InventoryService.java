package com.github.wenqigilantz.service.inventory.service;

import com.github.wenqigilantz.service.inventory.domain.ReservationStatus;
import com.github.wenqigilantz.service.inventory.persistence.entity.ProductEntity;
import com.github.wenqigilantz.service.inventory.persistence.entity.ReservationEntity;
import com.github.wenqigilantz.service.inventory.persistence.repository.ProductRepository;
import com.github.wenqigilantz.service.inventory.persistence.repository.ReservationRepository;
import com.github.wenqigilantz.service.inventory.web.dto.ProductResponse;
import com.github.wenqigilantz.service.inventory.web.dto.ReservationResponse;
import com.github.wenqigilantz.service.inventory.web.dto.ReserveRequest;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final ProductRepository productRepository;
    private final ReservationRepository reservationRepository;

    @Transactional(readOnly = true)
    public List<ProductResponse> listProducts() {
        return productRepository.findAll().stream()
                .map(this::toProductResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReservationResponse reserve(ReserveRequest request) {
        return reservationRepository.findById(request.getWorkflowId())
                .map(this::toReservationResponse)
                .orElseGet(() -> {
                    ProductEntity product = productRepository.findById(request.getSku())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SKU not found"));
                    if (product.getStock() < request.getQuantity()) {
                        return ReservationResponse.builder()
                                .workflowId(request.getWorkflowId())
                                .sku(request.getSku())
                                .quantity(request.getQuantity())
                                .status(ReservationStatus.RELEASED)
                                .success(false)
                                .build();
                    }
                    product.setStock(product.getStock() - request.getQuantity());
                    productRepository.save(product);

                    ReservationEntity reservation = new ReservationEntity();
                    reservation.setWorkflowId(request.getWorkflowId());
                    reservation.setSku(request.getSku());
                    reservation.setQuantity(request.getQuantity());
                    reservation.setStatus(ReservationStatus.ACTIVE);
                    reservationRepository.save(reservation);

                    return ReservationResponse.builder()
                            .workflowId(reservation.getWorkflowId())
                            .sku(reservation.getSku())
                            .quantity(reservation.getQuantity())
                            .status(reservation.getStatus())
                            .success(true)
                            .build();
                });
    }

    @Transactional
    public ReservationResponse release(String workflowId) {
        ReservationEntity reservation = reservationRepository.findById(workflowId).orElse(null);
        if (reservation == null) {
            return ReservationResponse.builder()
                    .workflowId(workflowId)
                    .success(true)
                    .status(ReservationStatus.RELEASED)
                    .build();
        }
        if (reservation.getStatus() == ReservationStatus.RELEASED) {
            return toReservationResponse(reservation);
        }
        ProductEntity product = productRepository.findById(reservation.getSku())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SKU not found"));
        product.setStock(product.getStock() + reservation.getQuantity());
        productRepository.save(product);
        reservation.setStatus(ReservationStatus.RELEASED);
        reservationRepository.save(reservation);
        return toReservationResponse(reservation);
    }

    private ProductResponse toProductResponse(ProductEntity entity) {
        return ProductResponse.builder()
                .sku(entity.getSku())
                .name(entity.getName())
                .stock(entity.getStock())
                .unitPrice(entity.getUnitPrice())
                .build();
    }

    private ReservationResponse toReservationResponse(ReservationEntity entity) {
        return ReservationResponse.builder()
                .workflowId(entity.getWorkflowId())
                .sku(entity.getSku())
                .quantity(entity.getQuantity())
                .status(entity.getStatus())
                .success(entity.getStatus() == ReservationStatus.ACTIVE)
                .build();
    }
}
