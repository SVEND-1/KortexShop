package org.example.kortex.orders.db;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findAllByUserId(Long userId);

    List<Order> findOrderByUserId(Long userId);

    @Query("""
    SELECT o.id FROM Order o
        WHERE (:userId IS NULL OR o.user = :userId)
    """)//если пользователь не указан то выдаст список полностью всех orders
    @EntityGraph(attributePaths = {"user"})
    List<Order> findPageFilter(Long userId,Pageable pageable);
}
