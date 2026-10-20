package com.github.wenqigilantz.service.orchestrator.config;

import com.github.wenqigilantz.service.orchestrator.workflow.PurchaseSagaWorkflow;
import com.github.wenqigilantz.service.orchestrator.workflow.activities.CancelOrderActivity;
import com.github.wenqigilantz.service.orchestrator.workflow.activities.ChargePaymentActivity;
import com.github.wenqigilantz.service.orchestrator.workflow.activities.ConfirmOrderActivity;
import com.github.wenqigilantz.service.orchestrator.workflow.activities.CreateOrderActivity;
import com.github.wenqigilantz.service.orchestrator.workflow.activities.RefundPaymentActivity;
import com.github.wenqigilantz.service.orchestrator.workflow.activities.ReleaseInventoryActivity;
import com.github.wenqigilantz.service.orchestrator.workflow.activities.ReserveInventoryActivity;
import io.dapr.workflows.runtime.WorkflowRuntime;
import io.dapr.workflows.runtime.WorkflowRuntimeBuilder;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WorkflowRuntimeLifecycle {

    private WorkflowRuntime runtime;

    @PostConstruct
    public void start() throws InterruptedException {
        Thread.sleep(3_000);
        WorkflowRuntimeBuilder builder = new WorkflowRuntimeBuilder()
                .registerWorkflow(PurchaseSagaWorkflow.class)
                .registerActivity(CreateOrderActivity.class)
                .registerActivity(ReserveInventoryActivity.class)
                .registerActivity(ChargePaymentActivity.class)
                .registerActivity(ConfirmOrderActivity.class)
                .registerActivity(CancelOrderActivity.class)
                .registerActivity(ReleaseInventoryActivity.class)
                .registerActivity(RefundPaymentActivity.class);
        runtime = builder.build();
        runtime.start(false);
        log.info("Dapr Workflow runtime started");
    }

    @PreDestroy
    public void stop() {
        log.info("Shutting down Dapr Workflow runtime");
    }
}
