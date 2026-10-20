package com.github.wenqigilantz.service.orchestrator.workflow;

import com.github.wenqigilantz.service.orchestrator.workflow.activities.CancelOrderActivity;
import com.github.wenqigilantz.service.orchestrator.workflow.activities.ChargePaymentActivity;
import com.github.wenqigilantz.service.orchestrator.workflow.activities.ConfirmOrderActivity;
import com.github.wenqigilantz.service.orchestrator.workflow.activities.CreateOrderActivity;
import com.github.wenqigilantz.service.orchestrator.workflow.activities.RefundPaymentActivity;
import com.github.wenqigilantz.service.orchestrator.workflow.activities.ReleaseInventoryActivity;
import com.github.wenqigilantz.service.orchestrator.workflow.activities.ReserveInventoryActivity;
import com.github.wenqigilantz.service.orchestrator.workflow.model.ChargeActivityInput;
import com.github.wenqigilantz.service.orchestrator.workflow.model.ChargeActivityResult;
import com.github.wenqigilantz.service.orchestrator.workflow.model.CreateOrderActivityInput;
import com.github.wenqigilantz.service.orchestrator.workflow.model.OrderActivityResult;
import com.github.wenqigilantz.service.orchestrator.workflow.model.OrderIdInput;
import com.github.wenqigilantz.service.orchestrator.workflow.model.ReserveActivityInput;
import com.github.wenqigilantz.service.orchestrator.workflow.model.ReserveActivityResult;
import com.github.wenqigilantz.service.orchestrator.workflow.model.SagaWorkflowInput;
import com.github.wenqigilantz.service.orchestrator.workflow.model.SagaWorkflowResult;
import com.github.wenqigilantz.service.orchestrator.workflow.model.WorkflowIdInput;
import io.dapr.workflows.Workflow;
import io.dapr.workflows.WorkflowStub;

public class PurchaseSagaWorkflow implements Workflow {

    @Override
    public WorkflowStub create() {
        return ctx -> {
            SagaWorkflowInput input = ctx.getInput(SagaWorkflowInput.class);
            String workflowId = ctx.getInstanceId();
            SagaWorkflowResult output = new SagaWorkflowResult();

            OrderActivityResult order = ctx.callActivity(
                    CreateOrderActivity.class.getName(),
                    new CreateOrderActivityInput(workflowId, input.getSku(), input.getQuantity(), input.getAmount()),
                    OrderActivityResult.class).await();
            output.setOrderId(order.getId());

            ReserveActivityResult reserve = ctx.callActivity(
                    ReserveInventoryActivity.class.getName(),
                    new ReserveActivityInput(workflowId, input.getSku(), input.getQuantity()),
                    ReserveActivityResult.class).await();

            if (!reserve.isSuccess()) {
                ctx.callActivity(
                        CancelOrderActivity.class.getName(),
                        new OrderIdInput(order.getId()),
                        OrderActivityResult.class).await();
                output.setProcessed(false);
                output.setCompensated(true);
                output.setMessage("Insufficient inventory");
                ctx.complete(output);
                return;
            }

            ChargeActivityResult payment = ctx.callActivity(
                    ChargePaymentActivity.class.getName(),
                    new ChargeActivityInput(workflowId, input.getAmount(), input.isSimulatePaymentFailure()),
                    ChargeActivityResult.class).await();

            if (!payment.isSuccess() || !payment.isCaptured()) {
                ctx.callActivity(
                        ReleaseInventoryActivity.class.getName(),
                        new WorkflowIdInput(workflowId),
                        String.class).await();
                ctx.callActivity(
                        CancelOrderActivity.class.getName(),
                        new OrderIdInput(order.getId()),
                        OrderActivityResult.class).await();
                output.setProcessed(false);
                output.setCompensated(true);
                output.setMessage("Payment failed");
                ctx.complete(output);
                return;
            }

            ctx.callActivity(
                    ConfirmOrderActivity.class.getName(),
                    new OrderIdInput(order.getId()),
                    OrderActivityResult.class).await();

            output.setProcessed(true);
            output.setCompensated(false);
            output.setMessage("Order completed");
            ctx.complete(output);
        };
    }
}
