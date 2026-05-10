package org.example.kortex.payments.db;

import lombok.*;
import org.example.kortex.users.db.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "payments")
public class PaymentEntity {//TODO сделать так чтобы один и тот же платеж нельзя было несколько раз использовать

    @Id
    @Column(name = "idempotency_key")
    private String idempotencyKey;

    @Column(name = "payment_id")
    private String paymentId;

    @Column(name = "receipt_id")
    private String receiptId;

    @Column
    private Boolean use;

    @ManyToOne
    private User user;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}