package com.github.wenqigilantz.service.orchestrator.workflow.activities;

import com.github.wenqigilantz.service.orchestrator.workflow.ActivitySupport;
import com.github.wenqigilantz.service.orchestrator.workflow.model.OrderActivityResult;
import com.github.wenqigilantz.service.orchestrator.workflow.model.OrderIdInput;
import io.dapr.workflows.WorkflowActivity;
import io.dapr.workflows.WorkflowActivityContext;
import java.util.Map;

public class ConfirmOrderActivity implements WorkflowActivity {

    @Override
    public Object run(WorkflowActivityContext ctx) {
        OrderIdInput input = ctx.getInput(OrderIdInput.class);
        try {
            return ActivitySupport.client().post(
                    ActivitySupport.commerce().getPurchaseOrderAppId(),
                    "/orders/" + input.getOrderId() + "/confirm",
                    Map.of(),
                    OrderActivityResult.class);
        } catch (Exception e) {
            throw new RuntimeException("ConfirmOrderActivity failed", e);
        }
    }
}
