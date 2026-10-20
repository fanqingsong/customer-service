# Workflow（Saga 电商）

三个业务微服务 + PostgreSQL，由 `workflow-orchestrator` 通过 Dapr Workflow 编排 Saga；`shop-ui` 提供下单界面。

## 目录

```
demos/workflow/
├── purchase-order-service/
├── inventory-service/
├── payment-service/
├── workflow-orchestrator/
├── shop-ui/
├── dapr-components/
├── docker/
├── docker-compose.yml
├── README-detail.md
├── README-workflow-durability.md
├── README-network-topology.md
└── quickstart.md
```

## 启动

```bash
docker compose -f demos/workflow/docker-compose.yml up --build
```

## 访问

- Shop UI: http://localhost:9500
- Orchestrator API: http://localhost:9400/api/products
- Zipkin: http://localhost:9411

- 业务与 Saga：[README-detail.md](./README-detail.md)
- **宕机恢复 / Workflow 持久化**：[README-workflow-durability.md](./README-workflow-durability.md)
- **网络与端口**：[README-network-topology.md](./README-network-topology.md)
