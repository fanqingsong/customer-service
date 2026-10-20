FROM swr.cn-north-4.myhuaweicloud.com/ddn-k8s/docker.io/maven:3.9-eclipse-temurin-17 AS build
WORKDIR /src
COPY pom.xml lombok.config ./
COPY demos ./demos
RUN mvn install -N -Dmaven.test.skip=true \
    && mvn -pl demos/发布订阅/customer-service -am package -Dmaven.test.skip=true

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /src/demos/发布订阅/customer-service/target/customer-service-exec.jar /app/customer-service-exec.jar
EXPOSE 9100
ENTRYPOINT ["java", "-jar", "/app/customer-service-exec.jar", "-p", "9100"]
