package org.example.kortex.carts.db;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);

    @EntityGraph(attributePaths = {"product"})
    @Query("SELECT DISTINCT ci FROM CartItem ci WHERE ci.cart.id = :cartId")
    List<CartItem> findAllByCartId(@Param("cartId") Long cartId);
}
