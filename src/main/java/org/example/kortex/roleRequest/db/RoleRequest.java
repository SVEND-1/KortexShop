package org.example.kortex.roleRequest.db;


import lombok.*;
import org.example.kortex.users.db.Role;
import org.example.kortex.users.db.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "role_requests")
public class RoleRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_role",nullable = false)
    private Role requestedRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_action",nullable = false)
    private TypeAction typeAction;

    @Column(length = 500)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name="status",nullable = false)
    private Status status = Status.PENDING;

    @Column(name = "create_at",nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Status {
        PENDING,
        APPROVED,
        REJECTED
    }

    public enum TypeAction{
        REMOVE,
        ENHANCE
    }
}
