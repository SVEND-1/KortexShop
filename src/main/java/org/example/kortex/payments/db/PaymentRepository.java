package org.example.kortex.payments.db;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
    Page<PaymentEntity> findAllByUserEmail(String userEmail, Pageable pageable);

    PaymentEntity findByPaymentId(String paymentId);//TODO СДелать OPTION
}
