package com.github.wenqigilantz.service.orchestrator.workflow.activities;

import com.github.wenqigilantz.service.orchestrator.dapr.InvocationResult;
import com.github.wenqigilantz.service.orchestrator.workflow.ActivitySupport;
import com.github.wenqigilantz.service.orchestrator.workflow.model.ChargeActivityInput;
import com.github.wenqigilantz.service.orchestrator.workflow.model.ChargeActivityResult;
import io.dapr.workflows.WorkflowActivity;
import io.dapr.workflows.WorkflowActivityContext;
import java.util.HashMap;
import java.util.Map;

public class ChargePaymentActivity implements WorkflowActivity {

    @Override
    public Object run(WorkflowActivityContext ctx) {
        ChargeActivityInput input = ctx.getInput(ChargeActivityInput.class);
        ChargeActivityResult result = new ChargeActivityResult();
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("workflowId", input.getWorkflowId());
            body.put("amount", input.getAmount());
            body.put("simulateFailure", input.isSimulateFailure());
            InvocationResult invocation = ActivitySupport.client().postAllowFailure(
                    ActivitySupport.commerce().getPaymentAppId(),
                    "/payments",
                    body,
                    Map.class);
            if (!invocation.isSuccess()) {
                result.setSuccess(false);
                result.setCaptured(false);
                result.setStatus("FAILED");
                return result;
            }
            Map<?, ?> response = (Map<?, ?>) invocation.getBody();
            String status = String.valueOf(response.get("status"));
            result.setSuccess(true);
            result.setCaptured("CAPTURED".equals(status));
            result.setStatus(status);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("ChargePaymentActivity failed", e);
        }
    }
}
