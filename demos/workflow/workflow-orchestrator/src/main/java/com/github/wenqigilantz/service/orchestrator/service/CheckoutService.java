package com.github.wenqigilantz.service.orchestrator.service;

import com.github.wenqigilantz.service.orchestrator.config.CommerceProperties;
import com.github.wenqigilantz.service.orchestrator.dapr.DaprInvocationClient;
import com.github.wenqigilantz.service.orchestrator.web.dto.CheckoutRequest;
import com.github.wenqigilantz.service.orchestrator.web.dto.CheckoutStartResponse;
import com.github.wenqigilantz.service.orchestrator.web.dto.CheckoutStatusResponse;
import com.github.wenqigilantz.service.orchestrator.web.dto.ProductView;
import com.github.wenqigilantz.service.orchestrator.workflow.PurchaseSagaWorkflow;
import com.github.wenqigilantz.service.orchestrator.workflow.model.SagaWorkflowInput;
import io.dapr.workflows.client.DaprWorkflowClient;
import io.dapr.workflows.client.WorkflowInstanceStatus;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final DaprInvocationClient daprInvocationClient;
    private final CommerceProperties commerceProperties;

    public List<ProductView> listProducts() {
        try {
            ProductView[] products = daprInvocationClient.getArray(
                    commerceProperties.getInventoryAppId(),
                    "/products",
                    ProductView[].class);
            return Arrays.stream(products).collect(Collectors.toList());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to load products", e);
        }
    }

    public CheckoutStartResponse startCheckout(CheckoutRequest request) {
        ProductView product = listProducts().stream()
                .filter(p -> p.getSku().equalsIgnoreCase(request.getSku()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown SKU"));

        SagaWorkflowInput input = new SagaWorkflowInput();
        input.setSku(product.getSku());
        input.setQuantity(request.getQuantity());
        input.setAmount(product.getUnitPrice() * request.getQuantity());
        input.setSimulatePaymentFailure(request.isSimulatePaymentFailure());

        try (DaprWorkflowClient client = new DaprWorkflowClient()) {
            String instanceId = client.scheduleNewWorkflow(PurchaseSagaWorkflow.class, input);
            return new CheckoutStartResponse(instanceId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to start workflow", e);
        }
    }

    public CheckoutStatusResponse getCheckoutStatus(String instanceId) {
        try (DaprWorkflowClient client = new DaprWorkflowClient()) {
            WorkflowInstanceStatus status = client.getInstanceState(instanceId, true);
            if (status == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Workflow instance not found");
            }
            Object order = null;
            Object payment = null;
            try {
                order = daprInvocationClient.get(
                        commerceProperties.getPurchaseOrderAppId(),
                        "/orders?workflowId=" + instanceId,
                        Map.class);
            } catch (Exception ignored) {
                // order may not exist yet
            }
            try {
                payment = daprInvocationClient.get(
                        commerceProperties.getPaymentAppId(),
                        "/payments/" + instanceId,
                        Map.class);
            } catch (Exception ignored) {
                // payment may not exist yet
            }
            return CheckoutStatusResponse.builder()
                    .instanceId(instanceId)
                    .runtimeStatus(String.valueOf(status.getRuntimeStatus()))
                    .serializedOutput(status.getSerializedOutput())
                    .order(order)
                    .payment(payment)
                    .build();
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to read workflow status", e);
        }
    }
}
