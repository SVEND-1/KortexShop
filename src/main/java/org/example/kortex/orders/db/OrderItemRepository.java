package org.example.kortex.orders.db;

import org.example.kortex.carts.db.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<CartItem> findByOrderId(Long orderId);

    Optional<OrderItem> findByOrderIdAndProductId(Long orderId, Long productId);

    void deleteByOrderIdAndProductId(Long cartId, Long productId);
}
