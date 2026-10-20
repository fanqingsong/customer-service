package com.github.wenqigilantz.service.purchaseorder.web.dto;

import com.github.wenqigilantz.service.purchaseorder.domain.OrderStatus;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class OrderResponse {

    UUID id;
    String workflowId;
    String sku;
    int quantity;
    long amount;
    OrderStatus status;
    Instant createdAt;
}
