package com.github.wenqigilantz.service.orchestrator.workflow;

import com.github.wenqigilantz.service.orchestrator.config.CommerceProperties;
import com.github.wenqigilantz.service.orchestrator.dapr.DaprInvocationClient;
import org.springframework.stereotype.Component;

@Component
public class ActivitySupport {

    private static DaprInvocationClient invocationClient;
    private static CommerceProperties commerceProperties;

    public ActivitySupport(DaprInvocationClient invocationClient, CommerceProperties commerceProperties) {
        ActivitySupport.invocationClient = invocationClient;
        ActivitySupport.commerceProperties = commerceProperties;
    }

    public static DaprInvocationClient client() {
        return invocationClient;
    }

    public static CommerceProperties commerce() {
        return commerceProperties;
    }
}
