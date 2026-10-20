package com.github.wenqigilantz.service.payment.web;

import com.github.wenqigilantz.service.payment.service.PaymentService;
import com.github.wenqigilantz.service.payment.web.dto.ChargeRequest;
import com.github.wenqigilantz.service.payment.web.dto.PaymentResponse;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public PaymentResponse charge(@Valid @RequestBody ChargeRequest request) {
        return paymentService.charge(request);
    }

    @PostMapping("/{workflowId}/refund")
    public PaymentResponse refund(@PathVariable String workflowId) {
        return paymentService.refund(workflowId);
    }

    @GetMapping("/{workflowId}")
    public PaymentResponse get(@PathVariable String workflowId) {
        return paymentService.get(workflowId);
    }
}
