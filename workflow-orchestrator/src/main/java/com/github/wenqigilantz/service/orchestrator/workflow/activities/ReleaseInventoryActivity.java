package com.github.wenqigilantz.service.orchestrator.workflow.activities;

import com.github.wenqigilantz.service.orchestrator.workflow.ActivitySupport;
import com.github.wenqigilantz.service.orchestrator.workflow.model.WorkflowIdInput;
import io.dapr.workflows.WorkflowActivity;
import io.dapr.workflows.WorkflowActivityContext;
import java.util.Map;

public class ReleaseInventoryActivity implements WorkflowActivity {

    @Override
    public Object run(WorkflowActivityContext ctx) {
        WorkflowIdInput input = ctx.getInput(WorkflowIdInput.class);
        try {
            ActivitySupport.client().post(
                    ActivitySupport.commerce().getInventoryAppId(),
                    "/reservations/" + input.getWorkflowId() + "/release",
                    Map.of(),
                    Object.class);
            return "released";
        } catch (Exception e) {
            throw new RuntimeException("ReleaseInventoryActivity failed", e);
        }
    }
}
