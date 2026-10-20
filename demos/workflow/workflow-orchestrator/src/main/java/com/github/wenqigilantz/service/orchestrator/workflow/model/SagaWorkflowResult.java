package com.github.wenqigilantz.service.orchestrator.workflow.model;

import lombok.Data;

@Data
public class SagaWorkflowResult {

    private boolean processed;
    private boolean compensated;
    private String message;
    private String orderId;
}
