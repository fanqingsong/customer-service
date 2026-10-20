package com.github.wenqigilantz.service.orderservice.web;

import com.github.wenqigilantz.service.orderservice.event.CustomerEvent;
import com.github.wenqigilantz.service.orderservice.service.CustomerEventHandler;
import io.dapr.Topic;
import io.dapr.client.domain.CloudEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CustomerEventSubscriber {

    private static final String PUBSUB_NAME = "customer-order-integration";
    private static final String TOPIC_ORDER_SERVICE = "order-service";

    private final CustomerEventHandler customerEventHandler;

    @Topic(name = TOPIC_ORDER_SERVICE, pubsubName = PUBSUB_NAME)
    @PostMapping(path = "/order-service", consumes = MediaType.ALL_VALUE)
    public Mono<ResponseEntity<String>> onCustomerEvent(
            @RequestBody(required = false) CloudEvent<CustomerEvent> cloudEvent) {
        return Mono.fromSupplier(() -> {
            if (cloudEvent == null || cloudEvent.getData() == null) {
                log.warn("Received Dapr message without data payload");
                return ResponseEntity.ok("SUCCESS");
            }
            CustomerEvent event = cloudEvent.getData();
            log.info(
                    "Subscriber received: customerId={}, status={}, firstName={}, lastName={}",
                    event.getCustomerId(),
                    event.getStatus(),
                    event.getFirstName(),
                    event.getLastName());
            customerEventHandler.handle(event);
            return ResponseEntity.ok("SUCCESS");
        });
    }
}
