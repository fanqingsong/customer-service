# Customer & Order Services

![customer-order.jpg](customer-order.jpg)

两个独立服务通过 Dapr **Pub/Sub** 与 **Service Invocation**（本地 Docker 使用 Redis + Placement）集成。

详细设计（What / Why / How 与关键代码）见独立文档：

- [发布订阅（Pub/Sub）](./docs/dapr-pubsub.md)
- [服务调用（Service Invocation）](./docs/dapr-service-invocation.md)

| 服务 | 端口 | 职责 |
|------|------|------|
| **customer-service** | 9100 | 客户 CRUD REST API，发布客户生命周期事件 |
| **order-service** | 9200 | 订阅 `order-service` 主题；经 Dapr 调用 customer-service 查询客户 |

### 目录结构

```
.
├── customer-service/          # Customer Service（单模块 Spring Boot）
│   ├── src/main/java/         # 按包分层：data / persistence / service / restcontroller
│   ├── src/main/resources/
│   └── Dockerfile
├── order-service/
├── dapr-components/
├── docker-compose.yml
└── pom.xml
```

Customer Service 发布的事件：`CustomerWasCreated`、`CustomerWasUpdated`、`CustomerWasDeleted`。

### 前置条件

* Java 17
* Maven 3.9+
* Docker（使用 `docker compose`）

### 本地构建

```bash
mvn clean install -DskipTests
```

### Docker Compose 启动（推荐）

```bash
docker compose up --build
```

* Customer API / Swagger: http://localhost:9100/swagger-ui.html
* Order Service 健康检查: http://localhost:9200/actuator/health

停止：

```bash
docker compose down
```

### 创建客户报 `Connection refused: 127.0.0.1:50001`

Customer Service 通过 **Dapr sidecar**（gRPC `50001`）发 Pub/Sub 事件。Compose 里 sidecar 使用 `network_mode: service:customer-service`，与 app **共用网络**。

若只单独重建/重启了 `customer-service` 容器，sidecar 可能仍挂在**旧容器 ID** 上，应用里访问 `127.0.0.1:50001` 会失败，Swagger 创建客户会 500。

请**成对**重建 app 与 sidecar：

```bash
docker compose up -d --force-recreate customer-service customer-service-dapr
```

日常请用 `docker compose up --build` 启动全栈，避免只 `docker restart customer-service`。

### 仅本地运行 Customer Service + Dapr

```bash
dapr run --components-path ./dapr-components --app-id customer-service --app-port 9100 -- \
  java -jar customer-service/target/customer-service-exec.jar -p 9100
```

Pub/Sub 组件见 `dapr-components/pubsub-redis.yaml`。Order Service 通过 Dapr Java SDK 的 `@Topic` 注解声明订阅（与 [Pub/Sub 快速入门](https://docs.dapr.io/getting-started/quickstarts/pubsub-quickstart/) 一致），无需单独的 Subscription YAML。

### Service Invocation（服务调用）

实现方式与 [Service Invocation 快速入门](https://docs.dapr.io/getting-started/quickstarts/serviceinvocation-quickstart/) 一致：Order Service 访问**本机 Dapr sidecar**（默认 `127.0.0.1:3500`），在请求头中设置 `dapr-app-id: customer-service`，由 sidecar 转发到 customer-service 的 `GET /customers/{customerId}`。

- 收到 Pub/Sub 事件后，`CustomerEventHandler` 会再调用 customer-service 拉取最新客户信息（日志中出现 `Service invocation passed`）。
- 也可直接请求 Order Service 代理接口验证：

```bash
curl http://localhost:9200/customers/{customerId}
```

Compose 中增加了 **placement** 服务，并为 sidecar 配置了 `-placement-host-address` 与 `-dapr-listen-addresses 0.0.0.0`，以便容器间服务发现（参见 [Dapr Docker Compose 文档](https://docs.dapr.io/operations/hosting/self-hosted/self-hosted-with-docker/)）。

本地仅 Dapr 运行时（非 Compose）可先启动 customer-service sidecar，再启动 order-service sidecar，环境变量 `DAPR_HTTP_PORT` 与快速入门中 checkout 服务相同。
