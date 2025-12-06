package org.example.kortex.users.db;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "role_requests")
public class RoleRequest {//Добавить снять или повысить

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_role",nullable = false)
    private User.Role requestedRole;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public TypeAction getTypeAction() {
        return typeAction;
    }

    public void setTypeAction(TypeAction typeAction) {
        this.typeAction = typeAction;
    }

    public User.Role getRequestedRole() {
        return requestedRole;
    }

    public void setRequestedRole(User.Role requestedRole) {
        this.requestedRole = requestedRole;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public RoleRequest() {}

    public RoleRequest(User user, User.Role requestedRole, String message) {
        this.user = user;
        this.requestedRole = requestedRole;
        this.message = message;
    }
}
