package com.github.wenqigilantz.service.payment.persistence.repository;

import com.github.wenqigilantz.service.payment.persistence.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<PaymentEntity, String> {
}
