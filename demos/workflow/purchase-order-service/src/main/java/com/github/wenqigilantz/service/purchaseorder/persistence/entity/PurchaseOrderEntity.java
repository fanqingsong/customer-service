package com.github.wenqigilantz.service.purchaseorder.persistence.entity;

import com.github.wenqigilantz.service.purchaseorder.domain.OrderStatus;
import java.time.Instant;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "orders")
@Getter
@Setter
public class PurchaseOrderEntity {

    @Id
    private UUID id;

    @Column(name = "workflow_id", nullable = false, unique = true)
    private String workflowId;

    @Column(nullable = false)
    private String sku;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private long amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
