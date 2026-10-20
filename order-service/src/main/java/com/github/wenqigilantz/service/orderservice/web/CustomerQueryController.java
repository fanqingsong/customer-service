package com.github.wenqigilantz.service.orderservice.web;

import com.github.wenqigilantz.service.orderservice.client.CustomerServiceInvocationClient;
import com.github.wenqigilantz.service.orderservice.data.CustomerInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerQueryController {

    private final CustomerServiceInvocationClient customerServiceInvocationClient;

    @GetMapping("/{customerId}")
    public ResponseEntity<CustomerInfo> getCustomer(@PathVariable String customerId) throws Exception {
        return ResponseEntity.ok(customerServiceInvocationClient.getCustomer(customerId));
    }
}
