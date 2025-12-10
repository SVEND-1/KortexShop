package org.example.kortex.orders.db;

import org.aspectj.weaver.ast.Or;
import org.example.kortex.products.db.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findOrderByUserId(Long userId);

    @EntityGraph(attributePaths = {"orderItems", "orderItems.product"})
    @Query("SELECT DISTINCT o FROM Order o WHERE o.id = :id")
    Order findByIdWithItems(@Param("id") Long id);

    // Оптимизированный: заказ с пользователем, курьером, товарами и продуктами
    @EntityGraph(attributePaths = {
            "user",
            "courier",
            "orderItems",
            "orderItems.product"
    })
    @Query("SELECT DISTINCT o FROM Order o WHERE o.id = :id")
    Order findByIdWithAll(@Param("id") Long id);

    @EntityGraph(attributePaths = {"orderItems", "orderItems.product", "courier"})
    @Query("SELECT DISTINCT o FROM Order o WHERE o.user.id = :userId ORDER BY o.orderDate DESC")
    List<Order> findOrdersByUserId(@Param("userId") Long userId);

    @EntityGraph(attributePaths = {"orderItems", "orderItems.product", "user"})
    @Query("SELECT DISTINCT o FROM Order o WHERE o.courier.id = :courierId ORDER BY o.orderDate DESC")
    List<Order> assignedOrdersPage(@Param("courierId") Long courierId,
                               Pageable pageable);

    @EntityGraph(attributePaths = {"orderItems", "orderItems.product", "user"})
    @Query("SELECT DISTINCT o FROM Order o WHERE o.courier.id = :courierId ORDER BY o.orderDate DESC")
    List<Order> assignedOrders(@Param("courierId") Long courierId);

    @EntityGraph(attributePaths = {"orderItems", "orderItems.product", "user"})
    @Query("SELECT DISTINCT o FROM Order o WHERE o.courier IS NULL AND o.status = 'PENDING' ORDER BY o.orderDate DESC")
    List<Order> availableOrdersPage(Pageable pageable);

}
