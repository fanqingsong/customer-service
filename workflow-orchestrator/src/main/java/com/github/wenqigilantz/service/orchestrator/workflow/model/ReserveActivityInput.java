package com.github.wenqigilantz.service.orchestrator.workflow.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReserveActivityInput {

    private String workflowId;
    private String sku;
    private int quantity;
}
