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

/**
 * 购买 Saga 编排：由 Dapr Workflow 持久化执行，Activity 经 Sidecar 调用三个 Commerce 微服务。
 *
 * <h2>在系统中的位置</h2>
 * <pre>
 *   shop-ui ──POST /api/checkout──► workflow-orchestrator
 *                                        │
 *                                        ▼
 *                              PurchaseSagaWorkflow (本类)
 *                                        │
 *                    ┌───────────────────┼───────────────────┐
 *                    ▼                   ▼                   ▼
 *         purchase-order-service   inventory-service   payment-service
 *              (订单 PENDING…)         (预留/释放)          (扣款/失败)
 * </pre>
 *
 * <h2>正向步骤与补偿（Choreography 式 Saga，失败时 LIFO 回滚已完成的步骤）</h2>
 * <pre>
 *   步骤 │ 正向 Activity              │ 服务侧效果           │ 失败时的补偿（逆序）
 *   ─────┼────────────────────────────┼──────────────────────┼─────────────────────────
 *    1   │ CreateOrderActivity        │ 订单 PENDING         │ CancelOrder → CANCELLED
 *    2   │ ReserveInventoryActivity   │ 库存预留             │ ReleaseInventory
 *    3   │ ChargePaymentActivity      │ 支付 CAPTURED        │ Release + Cancel（未扣款则无需 Refund）
 *    4   │ ConfirmOrderActivity       │ 订单 CONFIRMED       │ （成功路径无补偿）
 * </pre>
 *
 * <h2>三条执行路径（状态机示意）</h2>
 * <pre>
 *                    ┌──────────────┐
 *                    │  读取 Input  │
 *                    │ workflowId   │
 *                    └──────┬───────┘
 *                           │
 *                    [1] 创建订单
 *                           │
 *                    [2] 预留库存
 *                           │
 *              ┌────────────┴────────────┐
 *              │ reserve.success?        │
 *              └────────────┬────────────┘
 *                    no     │     yes
 *              ┌────────────┘     └────────────┐
 *              ▼                               ▼
 *        补偿: 取消订单                   [3] 扣款
 *        processed=false                         │
 *        compensated=true              ┌───────┴───────┐
 *                                       │ 支付成功?     │
 *                                       └───────┬───────┘
 *                                         no    │    yes
 *                                   ┌───────────┘    └──────────┐
 *                                   ▼                           ▼
 *                          补偿: 释放库存 + 取消订单        [4] 确认订单
 *                          processed=false                 processed=true
 *                          compensated=true               compensated=false
 * </pre>
 *
 * <p>跨服务关联键：{@code ctx.getInstanceId()} 作为 {@code workflowId} 写入订单/库存/支付记录，
 * 便于幂等补偿与排查。编排入口见 {@code CheckoutService#scheduleNewWorkflow}。</p>
 *
 * <p>说明：{@link RefundPaymentActivity} 已在运行时注册，供扩展「已扣款后需退款」等场景；
 * 当前支付失败分支未调用退款（扣款未 CAPTURED）。</p>
 *
 * @see com.github.wenqigilantz.service.orchestrator.config.WorkflowRuntimeLifecycle
 */
public class PurchaseSagaWorkflow implements Workflow {

    @Override
    public WorkflowStub create() {
        return ctx -> {
            // ── 初始化：Workflow 输入 + 实例 ID（全 Saga 的 business correlation id）──
            SagaWorkflowInput input = ctx.getInput(SagaWorkflowInput.class);
            String workflowId = ctx.getInstanceId();
            SagaWorkflowResult output = new SagaWorkflowResult();

            // ── Step 1：创建订单（purchase-order-service，状态 PENDING）──
            OrderActivityResult order = ctx.callActivity(
                    CreateOrderActivity.class.getName(),
                    new CreateOrderActivityInput(workflowId, input.getSku(), input.getQuantity(), input.getAmount()),
                    OrderActivityResult.class).await();
            output.setOrderId(order.getId());

            // ── Step 2：预留库存（inventory-service）──
            ReserveActivityResult reserve = ctx.callActivity(
                    ReserveInventoryActivity.class.getName(),
                    new ReserveActivityInput(workflowId, input.getSku(), input.getQuantity()),
                    ReserveActivityResult.class).await();

            // 分支 A：库存不足 → 仅回滚 Step 1（尚未扣款，无需 Release / Refund）
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

            // ── Step 3：扣款（payment-service；simulatePaymentFailure 时返回失败供演示补偿）──
            ChargeActivityResult payment = ctx.callActivity(
                    ChargePaymentActivity.class.getName(),
                    new ChargeActivityInput(workflowId, input.getAmount(), input.isSimulatePaymentFailure()),
                    ChargeActivityResult.class).await();

            // 分支 B：支付失败或未 CAPTURED → LIFO：先释放库存(2)，再取消订单(1)
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

            // ── Step 4：确认订单（purchase-order-service → CONFIRMED）──
            ctx.callActivity(
                    ConfirmOrderActivity.class.getName(),
                    new OrderIdInput(order.getId()),
                    OrderActivityResult.class).await();

            // 分支 C：Happy path
            output.setProcessed(true);
            output.setCompensated(false);
            output.setMessage("Order completed");
            ctx.complete(output);
        };
    }
}
