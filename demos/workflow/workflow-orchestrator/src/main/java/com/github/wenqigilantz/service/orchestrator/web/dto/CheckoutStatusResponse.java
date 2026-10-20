package com.github.wenqigilantz.service.orchestrator.web.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CheckoutStatusResponse {

    String instanceId;
    String runtimeStatus;
    String serializedOutput;
    Object order;
    Object payment;
    Object product;
}
