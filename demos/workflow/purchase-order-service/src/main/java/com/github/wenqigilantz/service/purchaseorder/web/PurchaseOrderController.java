package com.github.wenqigilantz.service.purchaseorder.web;

import com.github.wenqigilantz.service.purchaseorder.service.PurchaseOrderService;
import com.github.wenqigilantz.service.purchaseorder.web.dto.CreateOrderRequest;
import com.github.wenqigilantz.service.purchaseorder.web.dto.OrderResponse;
import java.util.UUID;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@Valid @RequestBody CreateOrderRequest request) {
        return purchaseOrderService.createOrder(request);
    }

    @GetMapping("/{id}")
    public OrderResponse getById(@PathVariable UUID id) {
        return purchaseOrderService.getById(id);
    }

    @GetMapping
    public OrderResponse getByWorkflowId(@RequestParam String workflowId) {
        return purchaseOrderService.getByWorkflowId(workflowId);
    }

    @PostMapping("/{id}/confirm")
    public OrderResponse confirm(@PathVariable UUID id) {
        return purchaseOrderService.confirm(id);
    }

    @PostMapping("/{id}/cancel")
    public OrderResponse cancel(@PathVariable UUID id) {
        return purchaseOrderService.cancel(id);
    }
}
