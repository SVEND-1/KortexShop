package org.example.kortex.users.db;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT DISTINCT u FROM User u WHERE u.email = LOWER(:email)")
    User findByEmailEqualsIgnoreCase(@Param("email") String email);

    @EntityGraph(attributePaths = {"cart","cart.cartItems","cart.cartItems.product"})
    @Query("""
    SELECT DISTINCT u FROM User u 
    WHERE u.email = :email
""")
    User findByIdWithCart(@Param("email") String email);

    @EntityGraph(attributePaths = {"orders","orders.orderItems","orders.orderItems.product", "orders.courier"})
    @Query("""
    SELECT DISTINCT u FROM User u
    WHERE u.email = :email
""")
    User findByIdWithOrders(@Param("email") String email);

    // Оптимизированный: полная загрузка пользователя
    @EntityGraph(attributePaths = {
            "cart", "cart.cartItems", "cart.cartItems.product",
            "orders", "orders.orderItems", "orders.orderItems.product",
            "roleRequests"
    })
    @Query("SELECT DISTINCT u FROM User u WHERE u.email = :email")
    User findByIdWithEverything(@Param("email") String email);

    @EntityGraph(attributePaths = {"roleRequests"})
    @Query("""
    SELECT DISTINCT u FROM User u
    WHERE u.email = :email
""")
    User findByIdWithRoleRequests(@Param("email") String email);
}
