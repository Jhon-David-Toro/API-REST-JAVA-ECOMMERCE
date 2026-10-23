package com.david.ecommerce_api.order;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserIdOrderByIdDesc(Long userId);

    List<Order> findAllByOrderByIdDesc();

    List<Order> findByStatusOrderByIdDesc(OrderStatus status);
}
