package com.github.wenqigilantz.service.payment.web.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChargeRequest {

    @NotBlank
    private String workflowId;

    @Min(0)
    private long amount;

    private boolean simulateFailure;
}
