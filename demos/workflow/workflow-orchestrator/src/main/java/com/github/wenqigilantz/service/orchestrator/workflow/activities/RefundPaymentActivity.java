package com.github.wenqigilantz.service.orchestrator.workflow.activities;

import com.github.wenqigilantz.service.orchestrator.workflow.ActivitySupport;
import com.github.wenqigilantz.service.orchestrator.workflow.model.WorkflowIdInput;
import io.dapr.workflows.WorkflowActivity;
import io.dapr.workflows.WorkflowActivityContext;
import java.util.Map;

public class RefundPaymentActivity implements WorkflowActivity {

    @Override
    public Object run(WorkflowActivityContext ctx) {
        WorkflowIdInput input = ctx.getInput(WorkflowIdInput.class);
        try {
            ActivitySupport.client().post(
                    ActivitySupport.commerce().getPaymentAppId(),
                    "/payments/" + input.getWorkflowId() + "/refund",
                    Map.of(),
                    Object.class);
            return "refunded";
        } catch (Exception e) {
            throw new RuntimeException("RefundPaymentActivity failed", e);
        }
    }
}
