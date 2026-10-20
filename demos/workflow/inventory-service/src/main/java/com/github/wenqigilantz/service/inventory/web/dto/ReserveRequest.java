package com.github.wenqigilantz.service.inventory.web.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReserveRequest {

    @NotBlank
    private String workflowId;

    @NotBlank
    private String sku;

    @Min(1)
    private int quantity;
}
