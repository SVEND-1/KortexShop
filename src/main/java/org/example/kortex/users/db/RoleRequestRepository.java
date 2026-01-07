package org.example.kortex.users.db;

import org.example.kortex.products.db.Product;
import org.example.kortex.users.api.RoleRequestFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface RoleRequestRepository  extends JpaRepository<RoleRequest, Long> {

    @EntityGraph(attributePaths = {"user"})
    @Query("""
        SELECT DISTINCT r FROM RoleRequest r
        WHERE (:userId IS NULL OR r.user.id = :userId)
""")
    RoleRequest findByUserId(@Param("userId") Long userId);

    @Query("""
        SELECT COUNT(r) > 0 FROM RoleRequest r 
        WHERE r.user.id = :userId 
        AND r.status = :status
    """)
    boolean existsByUserIdAndStatus(@Param("userId") Long userId,
                                    @Param("status") RoleRequest.Status status);

    @EntityGraph(attributePaths = {"user"})
    @Query("""
    SELECT r FROM RoleRequest r
    WHERE (:role IS NULL OR r.requestedRole = :role)
    AND (:status IS NULL OR r.status = :status)
    AND (:type IS NULL OR r.typeAction = :type)
    ORDER BY r.createdAt DESC
""")
    Page<RoleRequest> findSearchFilter(@Param("role")Role role,
                                   @Param("status")RoleRequest.Status status,
                                   @Param("type") RoleRequest.TypeAction type,
                                   Pageable pageable);

    List<RoleRequest> getAllByUserId(Long userId);

}
