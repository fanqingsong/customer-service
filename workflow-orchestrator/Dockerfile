FROM swr.cn-north-4.myhuaweicloud.com/ddn-k8s/docker.io/maven:3.9-eclipse-temurin-17 AS build
WORKDIR /src
COPY pom.xml lombok.config ./
COPY customer-service ./customer-service
COPY order-service ./order-service
COPY purchase-order-service ./purchase-order-service
COPY inventory-service ./inventory-service
COPY payment-service ./payment-service
COPY workflow-orchestrator ./workflow-orchestrator
RUN mvn -pl workflow-orchestrator package -Dmaven.test.skip=true

FROM swr.cn-north-4.myhuaweicloud.com/ddn-k8s/docker.io/eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /src/workflow-orchestrator/target/workflow-orchestrator-exec.jar /app/app.jar
EXPOSE 9400
ENV PORT=9400
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
