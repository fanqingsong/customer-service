package com.github.wenqigilantz.service.orderservice.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.wenqigilantz.service.orderservice.data.CustomerInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Invokes customer-service through the local Dapr sidecar HTTP proxy
 * ({@code dapr-app-id} header), as in the
 * <a href="https://docs.dapr.io/getting-started/quickstarts/serviceinvocation-quickstart/">Service Invocation quickstart</a>.
 */
@Component
@RequiredArgsConstructor
public class CustomerServiceInvocationClient {

    private static final String DAPR_APP_ID_HEADER = "dapr-app-id";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Value("${dapr.http.host:127.0.0.1}")
    private String daprHost;

    @Value("${dapr.http.port:3500}")
    private int daprHttpPort;

    @Value("${dapr.invocation.customer-service-app-id:customer-service}")
    private String customerServiceAppId;

    public CustomerInfo getCustomer(String customerId) throws Exception {
        URI uri = URI.create(
                String.format("http://%s:%d/customers/%s", daprHost, daprHttpPort, customerId));

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .header("Content-Type", "application/json")
                .header(DAPR_APP_ID_HEADER, customerServiceAppId)
                .timeout(Duration.ofSeconds(10))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IllegalStateException(
                    "Customer service invocation failed: HTTP "
                            + response.statusCode()
                            + ", body="
                            + response.body());
        }
        return objectMapper.readValue(response.body(), CustomerInfo.class);
    }
}
