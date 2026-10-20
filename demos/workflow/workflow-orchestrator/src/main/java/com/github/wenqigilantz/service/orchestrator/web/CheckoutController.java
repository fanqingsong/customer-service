package com.github.wenqigilantz.service.orchestrator.web;

import com.github.wenqigilantz.service.orchestrator.service.CheckoutService;
import com.github.wenqigilantz.service.orchestrator.web.dto.CheckoutRequest;
import com.github.wenqigilantz.service.orchestrator.web.dto.CheckoutStartResponse;
import com.github.wenqigilantz.service.orchestrator.web.dto.CheckoutStatusResponse;
import com.github.wenqigilantz.service.orchestrator.web.dto.ProductView;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;

    @GetMapping("/products")
    public List<ProductView> products() {
        return checkoutService.listProducts();
    }

    @PostMapping("/checkout")
    public CheckoutStartResponse checkout(@Valid @RequestBody CheckoutRequest request) {
        return checkoutService.startCheckout(request);
    }

    @GetMapping("/checkout/{instanceId}")
    public CheckoutStatusResponse status(@PathVariable String instanceId) {
        return checkoutService.getCheckoutStatus(instanceId);
    }
}
