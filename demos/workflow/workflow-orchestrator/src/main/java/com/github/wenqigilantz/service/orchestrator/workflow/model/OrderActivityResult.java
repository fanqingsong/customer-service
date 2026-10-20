package com.github.wenqigilantz.service.orchestrator.workflow.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderActivityResult {

    private String id;
    private String workflowId;
    private String status;
}
