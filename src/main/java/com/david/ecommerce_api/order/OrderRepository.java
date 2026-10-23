package com.david.ecommerce_api.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserIdOrderByIdDesc(Long userId);

    List<Order> findAllByOrderByIdDesc();

    List<Order> findByStatusOrderByIdDesc(OrderStatus status);

    @Query("select count(o) > 0 from Order o join o.items i where o.userId = :userId and i.productId = :productId and o.status <> com.david.ecommerce_api.order.OrderStatus.CANCELLED")
    boolean hasPurchased(@Param("userId") Long userId, @Param("productId") Long productId);
}
