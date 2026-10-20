# Dapr Workflow：订单 / 库存 / 支付 Saga

Commerce 垂直切片：三个独立微服务 + PostgreSQL 业务库，由 **workflow-orchestrator** 通过 Dapr Workflow 编排 Saga（失败时 LIFO 补偿）。前端 **shop-ui**（React）经 orchestrator 下单并轮询状态。

## 架构

```
shop-ui → workflow-orchestrator (/api/checkout)
              ↓ Dapr Workflow (PurchaseSagaWorkflow)
              ↓ Activities → Service Invocation (dapr-app-id)
    purchase-order-service | inventory-service | payment-service
              ↓                      ↓                    ↓
         purchase_order_db    inventory_db         payment_db
```

### Saga 步骤

| 顺序 | 正向 | 补偿（幂等） |
|------|------|--------------|
| 1 | 创建订单 `PENDING` | 取消 → `CANCELLED` |
| 2 | 预留库存 | 释放库存 |
| 3 | 支付 `CAPTURED` | 退款 → `REFUNDED` |
| 4 | 确认订单 `CONFIRMED` | — |

关联键：**workflow instanceId**（跨服务 `workflowId`）。

勾选「模拟支付失败」时，payment-service 返回 422，Workflow 释放库存并取消订单。

### 宕机恢复与持久化

编排进程或 Sidecar 重启后，**同一 workflow 实例** 会通过 **history 重放** 继续（非「从 Java 中断行续跑」）。组件分工、场景表与本 demo 幂等设计见 **[README-workflow-durability.md](./README-workflow-durability.md)**。

## 端口与网络

宿主机常用入口：Shop UI **9500**、Orchestrator **9400**、Zipkin **9411**。Sidecar、Redis、Placement、Scheduler 及组内 `127.0.0.1:3500` 等完整拓扑见 **[README-network-topology.md](./README-network-topology.md)**。

## 启动

```bash
docker compose up --build
```

打开 http://localhost:9500 ，选择商品下单；可勾选模拟支付失败观察补偿。

## 关键代码

- 编排：[PurchaseSagaWorkflow](./workflow-orchestrator/src/main/java/com/github/wenqigilantz/service/orchestrator/workflow/PurchaseSagaWorkflow.java)
- Activity 调用：[DaprInvocationClient](./workflow-orchestrator/src/main/java/com/github/wenqigilantz/service/orchestrator/dapr/DaprInvocationClient.java)
- UI 入口：[CheckoutController](./workflow-orchestrator/src/main/java/com/github/wenqigilantz/service/orchestrator/web/CheckoutController.java)

## 验证补偿

1. 正常下单：`CAR x1` → workflow 输出 `processed:true`；订单 `CONFIRMED`；库存减 1。
2. 模拟支付失败 → 输出 `compensated:true`；订单 `CANCELLED`；库存恢复；payment 为 `FAILED` 或不存在 `CAPTURED`。

## 参考

- [Dapr Workflow](https://docs.dapr.io/developing-applications/building-blocks/workflow/)
- [Workflow Quickstart](https://docs.dapr.io/getting-started/quickstarts/workflow-quickstart/)
