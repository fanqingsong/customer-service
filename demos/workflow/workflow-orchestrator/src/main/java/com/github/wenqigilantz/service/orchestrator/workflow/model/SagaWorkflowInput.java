package com.github.wenqigilantz.service.orchestrator.workflow.model;

import lombok.Data;

@Data
public class SagaWorkflowInput {

    private String sku;
    private int quantity;
    private long amount;
    private boolean simulatePaymentFailure;
}
