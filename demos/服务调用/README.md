# 服务调用（Service Invocation）

Order Service 经本机 Dapr sidecar（`dapr-app-id: customer-service`）同步调用 Customer Service 的 `GET /customers/{id}`。

本 demo **与「发布订阅」共用同一套应用**（`demos/发布订阅/customer-service`、`demos/发布订阅/order-service`），Compose 通过 include 引用发布订阅栈，便于单独按功能名启动与阅读文档。

## 关键代码（位于发布订阅目录）

| 文件 | 说明 |
|------|------|
| [CustomerServiceInvocationClient](../发布订阅/order-service/src/main/java/com/github/wenqigilantz/service/orderservice/client/CustomerServiceInvocationClient.java) | HTTP → sidecar + `dapr-app-id` |
| [CustomerQueryController](../发布订阅/order-service/src/main/java/com/github/wenqigilantz/service/orderservice/web/CustomerQueryController.java) | `GET /customers/{id}` 代理 |
| [CustomerEventHandler](../发布订阅/order-service/src/main/java/com/github/wenqigilantz/service/orderservice/service/CustomerEventHandler.java) | 收事件后再 invoke 拉最新客户 |

## 启动

```bash
docker compose -f demos/服务调用/docker-compose.yml up --build
```

## 验证

```bash
curl http://localhost:9200/customers/{customerId}
```

详细说明见 [README-detail.md](./README-detail.md)。
