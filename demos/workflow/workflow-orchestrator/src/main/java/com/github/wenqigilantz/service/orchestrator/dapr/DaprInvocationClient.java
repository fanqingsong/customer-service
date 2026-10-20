package com.github.wenqigilantz.service.orchestrator.dapr;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DaprInvocationClient {

    private static final String DAPR_APP_ID_HEADER = "dapr-app-id";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Value("${dapr.http.host:127.0.0.1}")
    private String daprHost;

    @Value("${dapr.http.port:3500}")
    private int daprHttpPort;

    public <T> T get(String appId, String path, Class<T> type) throws Exception {
        URI uri = URI.create(String.format("http://%s:%d%s", daprHost, daprHttpPort, path));
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .header(DAPR_APP_ID_HEADER, appId)
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(15))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new DaprInvocationException(response.statusCode(), response.body());
        }
        return objectMapper.readValue(response.body(), type);
    }

    public <T> T[] getArray(String appId, String path, Class<T[]> arrayType) throws Exception {
        URI uri = URI.create(String.format("http://%s:%d%s", daprHost, daprHttpPort, path));
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .header(DAPR_APP_ID_HEADER, appId)
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(15))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new DaprInvocationException(response.statusCode(), response.body());
        }
        return objectMapper.readValue(response.body(), arrayType);
    }

    public <B, T> T post(String appId, String path, B body, Class<T> type) throws Exception {
        String json = objectMapper.writeValueAsString(body);
        URI uri = URI.create(String.format("http://%s:%d%s", daprHost, daprHttpPort, path));
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .uri(uri)
                .header(DAPR_APP_ID_HEADER, appId)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(15))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new DaprInvocationException(response.statusCode(), response.body());
        }
        if (type == Void.class || response.body() == null || response.body().isBlank()) {
            return null;
        }
        return objectMapper.readValue(response.body(), type);
    }

    public <B> InvocationResult postAllowFailure(String appId, String path, B body, Class<?> successType)
            throws Exception {
        String json = objectMapper.writeValueAsString(body);
        URI uri = URI.create(String.format("http://%s:%d%s", daprHost, daprHttpPort, path));
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .uri(uri)
                .header(DAPR_APP_ID_HEADER, appId)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(15))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            return InvocationResult.failure(response.statusCode(), response.body());
        }
        Object parsed = objectMapper.readValue(response.body(), successType);
        return InvocationResult.success(parsed);
    }
}
