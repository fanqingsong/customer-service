package com.github.wenqigilantz.service.inventory.persistence.repository;

import com.github.wenqigilantz.service.inventory.persistence.entity.ReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<ReservationEntity, String> {
}
