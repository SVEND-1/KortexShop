package org.example.kortex.carts.db;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    // Оптимизированный: корзина с товарами и продуктами
    @EntityGraph(attributePaths = {"cartItems", "cartItems.product"})
    @Query("SELECT DISTINCT c FROM Cart c WHERE c.user.id = :userId")
    Cart findByUserIdWithItems(@Param("userId") Long userId);

    @Query("SELECT c FROM Cart c WHERE c.user.id = :userId")
    Cart findByUserId(@Param("userId") Long userId);


}
