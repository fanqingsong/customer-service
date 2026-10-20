# 发布订阅（Pub/Sub）

Customer Service 发布客户生命周期事件，Order Service 通过 Dapr `@Topic` 订阅。

## 目录

```
demos/发布订阅/
├── customer-service/     # 发布事件
├── order-service/        # 订阅 + 事件处理（亦用于「服务调用」demo）
├── dapr-components/
├── docker-compose.yml
└── README-detail.md      # 设计说明（What / Why / How）
```

## 启动

在仓库根目录：

```bash
docker compose -f demos/发布订阅/docker-compose.yml up --build
```

## 验证

- Customer Swagger: http://localhost:9100/swagger-ui.html
- 创建客户后查看 order-service 日志中的订阅处理

详细说明见 [README-detail.md](./README-detail.md)。
