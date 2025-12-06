package org.example.kortex.users.db;

import org.example.kortex.users.api.RoleRequestFilter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface RoleRequestRepository  extends JpaRepository<RoleRequest, Long> {

    @Query("""
    SELECT r FROM RoleRequest r
    WHERE (:role IS NULL OR r.requestedRole = :role)
    AND (:status IS NULL OR r.status = :status)
    AND (:type IS NULL OR r.typeAction = :type)
    ORDER BY r.createdAt DESC
""")
    List<RoleRequest> findSearchFilter(@Param("role")User.Role role,
                                       @Param("status")RoleRequest.Status status,
                                       @Param("type") RoleRequest.TypeAction type,
                                       Pageable pageable);
}
