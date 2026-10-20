package com.github.wenqigilantz.service.orchestrator.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductView {

    private String sku;
    private String name;
    private int stock;
    private long unitPrice;
}
