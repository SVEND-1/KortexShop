package org.example.kortex.orders.db;

import org.example.kortex.carts.db.CartItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // Оптимизированный: товары заказа с продуктами
    @EntityGraph(attributePaths = {"product"})
    @Query("SELECT DISTINCT oi FROM OrderItem oi WHERE oi.order.id = :orderId")
    List<OrderItem> findByOrderId(@Param("orderId") Long orderId);

    // Оптимизированный: все OrderItem с продуктами и заказами
    @EntityGraph(attributePaths = {"product", "order"})
    @Query("SELECT DISTINCT oi FROM OrderItem oi WHERE oi.order.id IN :orderIds")
    List<OrderItem> findByOrderIds(@Param("orderIds") List<Long> orderIds);
}

