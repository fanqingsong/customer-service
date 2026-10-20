package com.github.wenqigilantz.service.inventory.web.dto;

import com.github.wenqigilantz.service.inventory.domain.ReservationStatus;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ReservationResponse {

    String workflowId;
    String sku;
    int quantity;
    ReservationStatus status;
    boolean success;
}
