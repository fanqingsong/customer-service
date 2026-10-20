package com.github.wenqigilantz.service.orderservice.service;

import com.github.wenqigilantz.service.orderservice.client.CustomerServiceInvocationClient;
import com.github.wenqigilantz.service.orderservice.data.CustomerInfo;
import com.github.wenqigilantz.service.orderservice.event.CustomerEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerEventHandler {

    private final CustomerServiceInvocationClient customerServiceInvocationClient;

    public void handle(CustomerEvent event) {
        log.info(
                "Processing customer event: customerId={}, status={}, firstName={}, lastName={}",
                event.getCustomerId(),
                event.getStatus(),
                event.getFirstName(),
                event.getLastName());

        try {
            CustomerInfo customer = customerServiceInvocationClient.getCustomer(event.getCustomerId());
            log.info(
                    "Service invocation passed: customerId={}, firstName={}, lastName={}",
                    customer.getCustomerId(),
                    customer.getFirstName(),
                    customer.getLastName());
        } catch (Exception ex) {
            log.warn(
                    "Service invocation to customer-service failed for customerId={}: {}",
                    event.getCustomerId(),
                    ex.getMessage());
        }
    }
}
