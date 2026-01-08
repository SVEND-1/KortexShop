package org.example.kortex.orders.db;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @EntityGraph(attributePaths = {"product"})
    @Query("SELECT DISTINCT oi FROM OrderItem oi WHERE oi.order.id = :orderId")
    List<OrderItem> findByOrderId(@Param("orderId") Long orderId);

    @EntityGraph(attributePaths = {"product", "order"})
    @Query("SELECT DISTINCT oi FROM OrderItem oi WHERE oi.order.id IN :orderIds")
    List<OrderItem> findByOrderIds(@Param("orderIds") List<Long> orderIds);
}

