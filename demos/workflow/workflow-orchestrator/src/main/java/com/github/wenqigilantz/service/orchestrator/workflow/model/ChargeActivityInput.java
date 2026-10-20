package com.github.wenqigilantz.service.orchestrator.workflow.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChargeActivityInput {

    private String workflowId;
    private long amount;
    private boolean simulateFailure;
}
