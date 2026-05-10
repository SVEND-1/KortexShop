package org.example.kortex.payments.domain;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.ApiException;
import org.example.kortex.payments.api.dto.response.payment.PaymentCreateResponse;
import org.example.kortex.payments.api.dto.response.payment.PaymentPageResponse;
import org.example.kortex.payments.api.dto.response.payment.PaymentResponse;
import org.example.kortex.payments.api.dto.response.receipt.ReceiptResponse;
import org.example.kortex.payments.api.exception.PaymentOwnershipException;
import org.example.kortex.payments.db.PaymentEntity;
import org.example.kortex.payments.db.PaymentRepository;
import org.example.kortex.payments.domain.mapper.PaymentMapper;
import org.example.kortex.payments.domain.mapper.ReceiptMapper;
import org.example.kortex.users.domain.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.loolzaaa.youkassa.client.ApiClient;
import ru.loolzaaa.youkassa.client.ApiClientBuilder;
import ru.loolzaaa.youkassa.client.PaginatedResponse;
import ru.loolzaaa.youkassa.model.Payment;
import ru.loolzaaa.youkassa.model.Receipt;
import ru.loolzaaa.youkassa.model.Webhook;
import ru.loolzaaa.youkassa.processors.PaymentProcessor;
import ru.loolzaaa.youkassa.processors.ReceiptProcessor;
import ru.loolzaaa.youkassa.processors.WebhookProcessor;

import javax.annotation.PostConstruct;
import java.util.UUID;

@Service
@Slf4j
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final YooKassaManager yooKassaManager;
    private final PaymentManager paymentManager;
    private final ReceiptManager receiptManager;
    private final UserService userService;
    private final ReceiptMapper receiptMapper;
    @Value("${shop_id}")
    private String shopId;

    @Value("${payment_key}")
    private String secretKey;

    private ApiClient apiClient;

    private PaymentProcessor paymentProcessor;
    private ReceiptProcessor receiptProcessor;

    public PaymentService(PaymentRepository paymentRepository, PaymentMapper paymentMapper, YooKassaManager yooKassaManager, PaymentManager paymentManager, ReceiptManager receiptManager, UserService userService, ReceiptMapper receiptMapper) {
        this.paymentRepository = paymentRepository;
        this.paymentMapper = paymentMapper;
        this.yooKassaManager = yooKassaManager;
        this.paymentManager = paymentManager;
        this.receiptManager = receiptManager;
        this.userService = userService;
        this.receiptMapper = receiptMapper;
    }

    @PostConstruct
    public void init() {
        apiClient = ApiClientBuilder.newBuilder()
                .configureBasicAuth(shopId, secretKey)
                .build();
        paymentProcessor = new PaymentProcessor(apiClient);
        receiptProcessor = new ReceiptProcessor(apiClient);

        log.info("YooKassa инициализирована");
    }

    public PaymentResponse findPaymentDto(String paymentId){
        isValidUser(paymentId);
        return paymentMapper.convertEntityToPaymentResponse(findPayment(paymentId));
    }

    public PaymentPageResponse findAllPaymentsByUser(int page, int size) {
        return paymentMapper.toPageResponse(paymentManager.findAllPaymentsByUser(page,size));
    }

    public ReceiptResponse findReceipt(String paymentId){
        isValidUser(paymentId);
        return yooKassaManager.findReceiptDTO(receiptProcessor,paymentId);
    }

    @Transactional
    public PaymentCreateResponse createPayment(String summa,Long orderId) {//TODO если у фронтенда не получиться с оплатой сделатьь через куки
        String idempotencyKey = UUID.randomUUID().toString();
        try {
            Payment saved = yooKassaManager.createYooKassaPayment(paymentProcessor,idempotencyKey,summa,orderId);

            paymentManager.savePayment(idempotencyKey,saved);

            return new PaymentCreateResponse(
                    saved.getId(),
                    saved.getConfirmation().getConfirmationUrl(),
                    orderId
            );
        } catch (ApiException e) {
            log.error("Ошибка создания платежа: {}", e.getMessage());
            throw new RuntimeException("Не удалось создать платеж", e);
        }
    }

    @Transactional
    public ReceiptResponse createReceipt(String paymentId) {
        isValidUser(paymentId);
        try {
            Receipt saved = yooKassaManager.createYooKassaReceipt(receiptProcessor,paymentId);
            receiptManager.saveReceipt(paymentId,saved);
            return receiptMapper.convertReceiptToReceiptResponse(saved);
        } catch (ApiException e) {
            log.error("Ошибка создания чека для платежа {}: {}", paymentId, e.getMessage());
            throw new RuntimeException("Не удалось создать чек", e);
        }
    }


    public PaymentEntity findByPaymentId(String paymentId){
        return paymentRepository.findByPaymentId(paymentId);
    }

    public Payment findPayment(String paymentId) {
        return yooKassaManager.findPayment(paymentProcessor,paymentId);
    }

    public PaymentEntity save(PaymentEntity payment) {
        return paymentRepository.save(payment);
    }


    public void isValidUser(String paymentId) {
        if(!findByPaymentId(paymentId).getUser().getId().equals(userService.getCurrentUser().getId())){
            log.warn("Пользователь не является владельцем платежа");
            throw new PaymentOwnershipException("Пользователь не является владельцем платежа");
        }
    }
}
