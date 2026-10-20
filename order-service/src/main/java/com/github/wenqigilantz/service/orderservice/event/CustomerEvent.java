package com.github.wenqigilantz.service.orderservice.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerEvent {

    private String customerId;
    private String firstName;
    private String lastName;
    private String status;
}
