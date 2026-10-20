package com.github.wenqigilantz.service.inventory.persistence.repository;

import com.github.wenqigilantz.service.inventory.persistence.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<ProductEntity, String> {
}
