package com.github.wenqigilantz.service.purchaseorder.persistence.repository;

import com.github.wenqigilantz.service.purchaseorder.persistence.entity.PurchaseOrderEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrderEntity, UUID> {

    Optional<PurchaseOrderEntity> findByWorkflowId(String workflowId);
}
