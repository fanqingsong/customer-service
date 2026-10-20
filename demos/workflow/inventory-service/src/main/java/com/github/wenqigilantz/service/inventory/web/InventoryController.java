package com.github.wenqigilantz.service.inventory.web;

import com.github.wenqigilantz.service.inventory.service.InventoryService;
import com.github.wenqigilantz.service.inventory.web.dto.ProductResponse;
import com.github.wenqigilantz.service.inventory.web.dto.ReservationResponse;
import com.github.wenqigilantz.service.inventory.web.dto.ReserveRequest;
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
@RequestMapping
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/products")
    public List<ProductResponse> products() {
        return inventoryService.listProducts();
    }

    @PostMapping("/reservations")
    public ReservationResponse reserve(@Valid @RequestBody ReserveRequest request) {
        return inventoryService.reserve(request);
    }

    @PostMapping("/reservations/{workflowId}/release")
    public ReservationResponse release(@PathVariable String workflowId) {
        return inventoryService.release(workflowId);
    }
}
