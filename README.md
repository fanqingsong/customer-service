# Customer & Order Services

![customer-order.jpg](customer-order.jpg)

本仓库通过 **三个子 demo** 演示 Dapr 能力（本地 Docker：Redis + Placement + Scheduler）。每个子 demo 有独立目录、Compose 与文档。

| 子 demo | 目录 | 能力 |
|---------|------|------|
| **发布订阅** | [demos/发布订阅](./demos/发布订阅/) | Customer → Order 事件（Pub/Sub） |
| **服务调用** | [demos/服务调用](./demos/服务调用/) | Order → Customer 同步调用（Service Invocation） |
| **workflow** | [demos/workflow](./demos/workflow/) | Saga 电商 + React UI |

### 目录结构

```
.
├── demos/
│   ├── 发布订阅/          # customer-service、order-service
│   ├── 服务调用/          # 文档 + Compose（共用发布订阅应用）
│   └── workflow/          # 订单 / 库存 / 支付 / 编排 / shop-ui
├── pom.xml
└── docker-compose.yml     # 合并启动全部子 demo
```

### 前置条件

* Java 17
* Maven 3.9+
* Docker（使用 `docker compose`）

### 本地构建

```bash
mvn clean install -DskipTests
```

### 启动方式

**全栈（三个 demo 一起）：**

```bash
docker compose up --build
```

**只跑某一个子 demo：**

```bash
docker compose -f demos/发布订阅/docker-compose.yml up --build
docker compose -f demos/服务调用/docker-compose.yml up --build
docker compose -f demos/workflow/docker-compose.yml up --build
```

### 端口一览

| 服务 | 端口 | 所属 demo |
|------|------|-----------|
| customer-service | 9100 | 发布订阅 / 服务调用 |
| order-service | 9200 | 发布订阅 / 服务调用 |
| purchase-order-service | 9300 | workflow |
| inventory-service | 9310 | workflow |
| payment-service | 9320 | workflow |
| workflow-orchestrator | 9400 | workflow |
| shop-ui | 9500 | workflow |
| Zipkin | 9411 | 共用 |

停止：`docker compose down`

### 文档

- [发布订阅](./demos/发布订阅/README-detail.md)
- [服务调用](./demos/服务调用/README-detail.md)
- [Workflow Saga 电商](./demos/workflow/README-detail.md)

### 创建客户报 `Connection refused: 127.0.0.1:50001`

Customer Service 通过 Dapr sidecar（gRPC `50001`）发 Pub/Sub。若只单独重建了 app 容器，请成对重建 app 与 sidecar：

```bash
docker compose up -d --force-recreate customer-service customer-service-dapr
```

Compose 中 sidecar 使用 `network_mode: service:customer-service`，日常请用 `docker compose up --build` 启动，避免只 `docker restart customer-service`。
