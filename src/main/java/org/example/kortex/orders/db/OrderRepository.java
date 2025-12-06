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

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findAllByUserId(Long userId);

    List<Order> findOrderByUserId(Long userId);

    @Query("""
    SELECT o FROM Order o
        WHERE  o.user.id = :userId
    """)//если пользователь не указан то выдаст список полностью всех orders
    List<Order> findOrderByUser(@Param("userId") Long userId,
                               Pageable pageable);

    @Query("""
    SELECT o FROM Order o
    WHERE o.courier.id = :courierId
    ORDER BY o.orderDate DESC
""")
    List<Order> assignedOrdersPage(@Param("courierId") Long courierId,
                               Pageable pageable);

    @Query("""
    SELECT o FROM Order o
    WHERE o.courier.id = :courierId
""")
    List<Order> assignedOrders(@Param("courierId") Long courierId);

    @Query(
            """
    SELECT o FROM Order o
    WHERE o.courier == null 
    ORDER BY o.orderDate DESC
"""
    )
    List<Order> availableOrdersPage(Pageable pageable);

}
