package com.github.wenqiglantz.service.customerservice.service.impl;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

final class CustomerPageables {

    static final Set<String> SORTABLE_PROPERTIES = Set.of(
            "firstName",
            "lastName",
            "customerId",
            "id",
            "insertedAt",
            "updatedAt");

    private CustomerPageables() {
    }

    static Pageable sanitize(Pageable pageable) {
        Sort sort = pageable.getSort();
        if (sort.isUnsorted()) {
            return pageable;
        }

        Sort.Order[] validOrders = sort.stream()
                .filter(order -> SORTABLE_PROPERTIES.contains(order.getProperty()))
                .toArray(Sort.Order[]::new);

        if (validOrders.length == 0) {
            return PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(Sort.Direction.ASC, "firstName"));
        }

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(validOrders));
    }
}
