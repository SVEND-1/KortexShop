package org.example.kortex.products.db;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findProductByCategory(Product.Category category);

    List<Product> findByCountGreaterThan(int countIsGreaterThan);

    List<Product> findBySellerId(Long sellerId);

    Page<Product> findByCategoryAndCountGreaterThan(Product.Category category, int countIsGreaterThan, PageRequest pageRequest);

    Page<Product> findByCountGreaterThan( int countIsGreaterThan, PageRequest pageRequest);

    List<Product> findTop48ByCountGreaterThan(int countIsGreaterThan);

    @Query("""
       SELECT p FROM Product p
           WHERE (:category IS NULL OR p.category = :category)
                AND (:query IS NULL OR :query = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')))
                    AND p.count > 0
    """)
    List<Product> findProductsFilter(@Param("category") Product.Category category,
                                     @Param("query") String query,
                                     Pageable pageable);
}
