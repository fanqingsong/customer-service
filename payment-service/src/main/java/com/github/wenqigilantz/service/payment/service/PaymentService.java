package com.github.wenqigilantz.service.payment.service;

import com.github.wenqigilantz.service.payment.domain.PaymentStatus;
import com.github.wenqigilantz.service.payment.persistence.entity.PaymentEntity;
import com.github.wenqigilantz.service.payment.persistence.repository.PaymentRepository;
import com.github.wenqigilantz.service.payment.web.dto.ChargeRequest;
import com.github.wenqigilantz.service.payment.web.dto.PaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Transactional
    public PaymentResponse charge(ChargeRequest request) {
        return paymentRepository.findById(request.getWorkflowId())
                .map(this::toResponse)
                .orElseGet(() -> {
                    if (request.isSimulateFailure()) {
                        PaymentEntity failed = new PaymentEntity();
                        failed.setWorkflowId(request.getWorkflowId());
                        failed.setAmount(request.getAmount());
                        failed.setStatus(PaymentStatus.FAILED);
                        paymentRepository.save(failed);
                        throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Simulated payment failure");
                    }
                    PaymentEntity entity = new PaymentEntity();
                    entity.setWorkflowId(request.getWorkflowId());
                    entity.setAmount(request.getAmount());
                    entity.setStatus(PaymentStatus.CAPTURED);
                    paymentRepository.save(entity);
                    return PaymentResponse.builder()
                            .workflowId(entity.getWorkflowId())
                            .amount(entity.getAmount())
                            .status(entity.getStatus())
                            .success(true)
                            .build();
                });
    }

    @Transactional
    public PaymentResponse refund(String workflowId) {
        PaymentEntity entity = paymentRepository.findById(workflowId).orElse(null);
        if (entity == null || entity.getStatus() == PaymentStatus.FAILED) {
            return PaymentResponse.builder()
                    .workflowId(workflowId)
                    .success(true)
                    .status(PaymentStatus.REFUNDED)
                    .amount(0)
                    .build();
        }
        if (entity.getStatus() == PaymentStatus.REFUNDED) {
            return toResponse(entity);
        }
        entity.setStatus(PaymentStatus.REFUNDED);
        paymentRepository.save(entity);
        return toResponse(entity);
    }

    @Transactional(readOnly = true)
    public PaymentResponse get(String workflowId) {
        return paymentRepository.findById(workflowId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
    }

    private PaymentResponse toResponse(PaymentEntity entity) {
        return PaymentResponse.builder()
                .workflowId(entity.getWorkflowId())
                .amount(entity.getAmount())
                .status(entity.getStatus())
                .success(entity.getStatus() == PaymentStatus.CAPTURED)
                .build();
    }
}
