package com.github.wenqigilantz.service.inventory.web.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ProductResponse {

    String sku;
    String name;
    int stock;
    long unitPrice;
}
