package com.github.wenqigilantz.service.orchestrator.dapr;

public class DaprInvocationException extends RuntimeException {

    private final int statusCode;

    public DaprInvocationException(int statusCode, String body) {
        super("Dapr invocation failed: HTTP " + statusCode + ", body=" + body);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
