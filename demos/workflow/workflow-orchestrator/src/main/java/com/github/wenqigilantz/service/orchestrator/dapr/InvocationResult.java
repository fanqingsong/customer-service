package com.github.wenqigilantz.service.orchestrator.dapr;

import lombok.Value;

@Value
public class InvocationResult {

    boolean success;
    Object body;
    int statusCode;
    String errorBody;

    public static InvocationResult success(Object body) {
        return new InvocationResult(true, body, 200, null);
    }

    public static InvocationResult failure(int statusCode, String errorBody) {
        return new InvocationResult(false, null, statusCode, errorBody);
    }
}
