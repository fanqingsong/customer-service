package com.github.wenqigilantz.service.orchestrator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "commerce")
public class CommerceProperties {

    private String purchaseOrderAppId = "purchase-order-service";
    private String inventoryAppId = "inventory-service";
    private String paymentAppId = "payment-service";
}
