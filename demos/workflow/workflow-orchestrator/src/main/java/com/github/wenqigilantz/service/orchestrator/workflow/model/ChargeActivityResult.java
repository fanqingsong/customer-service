package com.github.wenqigilantz.service.orchestrator.workflow.model;

import lombok.Data;

@Data
public class ChargeActivityResult {

    private boolean success;
    private boolean captured;
    private String status;
}
