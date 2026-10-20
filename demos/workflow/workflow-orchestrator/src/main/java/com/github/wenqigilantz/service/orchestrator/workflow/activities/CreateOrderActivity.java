package com.github.wenqigilantz.service.orchestrator.workflow.activities;

import com.github.wenqigilantz.service.orchestrator.workflow.ActivitySupport;
import com.github.wenqigilantz.service.orchestrator.workflow.model.CreateOrderActivityInput;
import com.github.wenqigilantz.service.orchestrator.workflow.model.OrderActivityResult;
import io.dapr.workflows.WorkflowActivity;
import io.dapr.workflows.WorkflowActivityContext;
import java.util.HashMap;
import java.util.Map;

public class CreateOrderActivity implements WorkflowActivity {

    @Override
    public Object run(WorkflowActivityContext ctx) {
        CreateOrderActivityInput input = ctx.getInput(CreateOrderActivityInput.class);
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("workflowId", input.getWorkflowId());
            body.put("sku", input.getSku());
            body.put("quantity", input.getQuantity());
            body.put("amount", input.getAmount());
            OrderActivityResult result = ActivitySupport.client().post(
                    ActivitySupport.commerce().getPurchaseOrderAppId(),
                    "/orders",
                    body,
                    OrderActivityResult.class);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("CreateOrderActivity failed", e);
        }
    }
}
