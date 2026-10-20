package com.github.wenqigilantz.service.purchaseorder.service;

import com.github.wenqigilantz.service.purchaseorder.domain.OrderStatus;
import com.github.wenqigilantz.service.purchaseorder.persistence.entity.PurchaseOrderEntity;
import com.github.wenqigilantz.service.purchaseorder.persistence.repository.PurchaseOrderRepository;
import com.github.wenqigilantz.service.purchaseorder.web.dto.CreateOrderRequest;
import com.github.wenqigilantz.service.purchaseorder.web.dto.OrderResponse;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class PurchaseOrderService {

    private final PurchaseOrderRepository repository;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        return repository.findByWorkflowId(request.getWorkflowId())
                .map(this::toResponse)
                .orElseGet(() -> {
                    PurchaseOrderEntity entity = new PurchaseOrderEntity();
                    entity.setId(UUID.randomUUID());
                    entity.setWorkflowId(request.getWorkflowId());
                    entity.setSku(request.getSku());
                    entity.setQuantity(request.getQuantity());
                    entity.setAmount(request.getAmount());
                    entity.setStatus(OrderStatus.PENDING);
                    entity.setCreatedAt(Instant.now());
                    return toResponse(repository.save(entity));
                });
    }

    @Transactional(readOnly = true)
    public OrderResponse getById(UUID id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
    }

    @Transactional(readOnly = true)
    public OrderResponse getByWorkflowId(String workflowId) {
        return repository.findByWorkflowId(workflowId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
    }

    @Transactional
    public OrderResponse confirm(UUID id) {
        PurchaseOrderEntity entity = load(id);
        if (entity.getStatus() == OrderStatus.CONFIRMED) {
            return toResponse(entity);
        }
        if (entity.getStatus() == OrderStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Order already cancelled");
        }
        entity.setStatus(OrderStatus.CONFIRMED);
        return toResponse(repository.save(entity));
    }

    @Transactional
    public OrderResponse cancel(UUID id) {
        PurchaseOrderEntity entity = load(id);
        if (entity.getStatus() == OrderStatus.CANCELLED) {
            return toResponse(entity);
        }
        if (entity.getStatus() == OrderStatus.CONFIRMED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot cancel confirmed order");
        }
        entity.setStatus(OrderStatus.CANCELLED);
        return toResponse(repository.save(entity));
    }

    private PurchaseOrderEntity load(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
    }

    private OrderResponse toResponse(PurchaseOrderEntity entity) {
        return OrderResponse.builder()
                .id(entity.getId())
                .workflowId(entity.getWorkflowId())
                .sku(entity.getSku())
                .quantity(entity.getQuantity())
                .amount(entity.getAmount())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
