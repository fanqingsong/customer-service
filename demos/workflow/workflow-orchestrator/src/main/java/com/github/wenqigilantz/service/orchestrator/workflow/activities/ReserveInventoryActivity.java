package com.github.wenqigilantz.service.orchestrator.workflow.activities;

import com.github.wenqigilantz.service.orchestrator.workflow.ActivitySupport;
import com.github.wenqigilantz.service.orchestrator.workflow.model.ReserveActivityInput;
import com.github.wenqigilantz.service.orchestrator.workflow.model.ReserveActivityResult;
import io.dapr.workflows.WorkflowActivity;
import io.dapr.workflows.WorkflowActivityContext;
import java.util.HashMap;
import java.util.Map;

public class ReserveInventoryActivity implements WorkflowActivity {

    @Override
    public Object run(WorkflowActivityContext ctx) {
        ReserveActivityInput input = ctx.getInput(ReserveActivityInput.class);
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("workflowId", input.getWorkflowId());
            body.put("sku", input.getSku());
            body.put("quantity", input.getQuantity());
            Map<?, ?> response = ActivitySupport.client().post(
                    ActivitySupport.commerce().getInventoryAppId(),
                    "/reservations",
                    body,
                    Map.class);
            ReserveActivityResult result = new ReserveActivityResult();
            result.setSuccess(Boolean.TRUE.equals(response.get("success")));
            return result;
        } catch (Exception e) {
            throw new RuntimeException("ReserveInventoryActivity failed", e);
        }
    }
}
