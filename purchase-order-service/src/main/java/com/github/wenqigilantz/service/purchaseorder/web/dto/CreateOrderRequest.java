package com.github.wenqigilantz.service.purchaseorder.web.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateOrderRequest {

    @NotBlank
    private String workflowId;

    @NotBlank
    private String sku;

    @Min(1)
    private int quantity;

    @Min(0)
    private long amount;
}
