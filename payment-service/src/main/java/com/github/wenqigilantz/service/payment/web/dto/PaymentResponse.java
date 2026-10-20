package com.github.wenqigilantz.service.payment.web.dto;

import com.github.wenqigilantz.service.payment.domain.PaymentStatus;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PaymentResponse {

    String workflowId;
    long amount;
    PaymentStatus status;
    boolean success;
}
