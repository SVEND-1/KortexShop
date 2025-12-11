package org.example.kortex.products.db;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @EntityGraph(attributePaths = {"seller"})
    @Query("SELECT DISTINCT p FROM Product p WHERE p.seller.id = :sellerId")
    List<Product> findBySellerId(@Param("sellerId") Long sellerId);


    @EntityGraph(attributePaths = {"seller"})
    @Query("""
       SELECT DISTINCT p FROM Product p
           WHERE (:category IS NULL OR p.category = :category)
                AND (:query IS NULL OR :query = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')))
                    AND p.count > 0
    """)
    Page<Product> findProductsFilter(@Param("category") Product.Category category,
                                     @Param("query") String query,
                                     Pageable pageable);

    @EntityGraph(attributePaths = {"seller"})
    @Query("SELECT DISTINCT p FROM Product p")
    List<Product> findAllWithSeller();
}
