package com.github.wenqigilantz.service.orchestrator.web.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CheckoutRequest {

    @NotBlank
    private String sku;

    @Min(1)
    private int quantity;

    private boolean simulatePaymentFailure;
}
