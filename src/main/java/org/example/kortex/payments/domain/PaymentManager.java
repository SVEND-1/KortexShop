package org.example.kortex.payments.domain;

import lombok.extern.slf4j.Slf4j;
import org.example.kortex.payments.api.dto.response.payment.PaymentResponse;
import org.example.kortex.payments.db.PaymentEntity;
import org.example.kortex.payments.db.PaymentRepository;
import org.example.kortex.users.db.User;
import org.example.kortex.users.domain.UserService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import ru.loolzaaa.youkassa.model.Payment;

import java.time.LocalDateTime;

@Slf4j
@Component
public class PaymentManager {

    private final UserService userService;
    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;

    public PaymentManager(UserService userService,
                          PaymentRepository paymentRepository,@Lazy PaymentService paymentService) {
        this.userService = userService;
        this.paymentRepository = paymentRepository;
        this.paymentService = paymentService;
    }

    public void savePayment(String idempotencyKey, Payment saved) {
        try {
            PaymentEntity paymentEntity = PaymentEntity.builder()
                    .idempotencyKey(idempotencyKey)
                    .user(userService.getCurrentUser())
                    .paymentId(saved.getId())
                    .use(false)
                    .createdAt(LocalDateTime.now())
                    .build();

            paymentService.save(paymentEntity);
        }catch (Exception e) {
            log.error("Не удалось сохранить платеж paymentId={},ex={}", saved.getId(), e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }


    public Page<PaymentResponse> findAllPaymentsByUser(int page, int size) {
        try {
            User user = userService.getCurrentUser();

            Pageable pageable = PageRequest.of(page, size);

            Page<PaymentEntity> userPayments = paymentRepository
                    .findAllByUserEmail(user.getEmail(), pageable);

            return userPayments.map(el -> paymentService.findPaymentDto(el.getPaymentId()));
        }catch (Exception e) {
            log.error("Не удалось загрузить страницу с платежами, ex={}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
}
