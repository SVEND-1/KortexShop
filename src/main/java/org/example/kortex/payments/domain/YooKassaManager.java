//package org.example.tula.payments.domain;
//
//import lombok.extern.slf4j.Slf4j;
//import org.apache.kafka.common.errors.ApiException;
//import org.example.tula.payments.api.dto.response.receipt.ReceiptResponse;
//import org.example.tula.payments.db.PaymentEntity;
//import org.example.tula.payments.domain.mapper.ReceiptMapper;
//import org.example.tula.users.domain.UserService;
//import org.springframework.context.annotation.Lazy;
//import org.springframework.stereotype.Component;
//import ru.loolzaaa.youkassa.model.Payment;
//import ru.loolzaaa.youkassa.model.Receipt;
//import ru.loolzaaa.youkassa.pojo.*;
//import ru.loolzaaa.youkassa.processors.PaymentProcessor;
//import ru.loolzaaa.youkassa.processors.ReceiptProcessor;
//
//import java.util.List;
//
//@Slf4j
//@Component
//public class YooKassaManager {
//
//    private final PaymentService paymentService;
//    private final UserService userService;
//    //private final String RETURN_URL = "http://localhost:5173/receipts";
//    private final String RETURN_URL = "http://localhost:5173/succeeded-payment";
//    private final ReceiptMapper receiptMapper;
//
//    public YooKassaManager(@Lazy PaymentService paymentService, UserService userService, ReceiptMapper receiptMapper) {
//        this.paymentService = paymentService;
//        this.userService = userService;
//        this.receiptMapper = receiptMapper;
//    }
//
//
//    public Payment findPayment(PaymentProcessor paymentProcessor,String paymentId) {
//        try {
//            return paymentProcessor.findById(paymentId);
//        } catch (ApiException e) {
//            log.error("Ошибка поиска платежа {}: {}", paymentId, e.getMessage());
//            throw new RuntimeException("Платеж не найден", e);
//        }
//    }
//
//
//    public Payment createYooKassaPayment(PaymentProcessor paymentProcessor, String idempotencyKey){
//        try {
//            Amount amount = Amount.builder()
//                    .value("1.00")
//                    .currency(Currency.RUB)
//                    .build();
//
//            Confirmation confirmation = Confirmation.builder()
//                    .type(Confirmation.Type.REDIRECT)
//                    .returnUrl(RETURN_URL)
//                    .build();
//
//            Payment payment = Payment.builder()
//                    .amount(amount)
//                    .description("Оплата подписки Premium")
//                    .confirmation(confirmation)
//                    .capture(true)
//                    .build();
//
//            return paymentProcessor.create(payment, idempotencyKey);
//        }catch (Exception e){
//            log.error("Не удалось создать платеж yookassa,ex={}", e.getMessage());
//            throw new RuntimeException(e.getMessage(), e);
//        }
//    }
//
//
//    public Receipt createYooKassaReceipt(ReceiptProcessor receiptProcessor, String paymentId) {
//        try {
//            Payment payment = paymentService.findPayment(paymentId);
//            if (!"succeeded".equals(payment.getStatus())) {
//                log.warn("Чек создается для неуспешного платежа: {}", paymentId);
//            }
//
//            Customer customer = Customer.builder()
//                    .email(userService.getCurrentUser().getEmail())
//                    .build();
//
//            Amount paymentAmount = payment.getAmount();
//            Amount itemAmount = Amount.builder()
//                    .value(paymentAmount.getValue())
//                    .currency(paymentAmount.getCurrency())
//                    .build();
//
//            Item item = Item.builder()
//                    .description("Оплата подписки Premium")
//                    .amount(itemAmount)
//                    .vatCode(1) // Без НДС
//                    .quantity("1")
//                    .build();
//
//            Settlement settlement = Settlement.builder()
//                    .type(getSettlementType(payment))
//                    .amount(itemAmount)
//                    .build();
//
//            Receipt receipt = Receipt.builder()
//                    .type(Receipt.Type.PAYMENT)
//                    .paymentId(paymentId)
//                    .customer(customer)
//                    .items(List.of(item))
//                    .settlements(List.of(settlement))
//                    .send(true)
//                    .build();
//
//            return receiptProcessor.create(receipt, null);
//        }catch (Exception e){
//            log.error("Не удалось создать чек через yookassa,ex={}", e.getMessage());
//            throw new RuntimeException(e.getMessage(), e);
//        }
//    }
//
//    public ReceiptResponse findReceiptDTO(ReceiptProcessor receiptProcessor, String paymentId){
//        try {
//            PaymentEntity payment = paymentService.findByPaymentId(paymentId);
//            Receipt receipt = receiptProcessor.findById(payment.getReceiptId());
//            return receiptMapper.convertReceiptToReceiptResponse(receipt);
//        }
//        catch (Exception e){
//            log.error("Ошибка поиска чека,ex={}", e.getMessage());
//            throw new RuntimeException("Чек не найден", e);
//        }
//    }
//
//    private String getSettlementType(Payment payment) {
//        return switch (payment.getPaymentMethod().getType()) {
//            case "bank_card", "sberbank", "tinkoff_bank", "alpha_bank", "sbp" ->
//                    Settlement.Type.CASHLESS;
//            case "yoo_money", "qiwi" ->
//                    Settlement.Type.PREPAYMENT;
//            case "cash" ->
//                    Settlement.Type.PAYOUT;
//            case "bank_transfer" ->
//                    Settlement.Type.POSTPAYMENT;
//            default ->
//                    Settlement.Type.CASHLESS;
//        };
//    }
//
//
//}

package org.example.kortex.payments.domain;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.ApiException;
import org.example.kortex.payments.api.dto.response.receipt.ReceiptResponse;
import org.example.kortex.payments.db.PaymentEntity;
import org.example.kortex.payments.domain.mapper.ReceiptMapper;
import org.example.kortex.users.domain.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import ru.loolzaaa.youkassa.model.Payment;
import ru.loolzaaa.youkassa.model.Receipt;
import ru.loolzaaa.youkassa.pojo.*;
import ru.loolzaaa.youkassa.processors.PaymentProcessor;
import ru.loolzaaa.youkassa.processors.ReceiptProcessor;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class YooKassaManager {

    private final PaymentService paymentService;
    private final UserService userService;

    @Value("${app.base-url:http://localhost:8080/payments}")
    private String RETURN_URL;

    private final ReceiptMapper receiptMapper;

    public YooKassaManager(@Lazy PaymentService paymentService, UserService userService, ReceiptMapper receiptMapper) {
        this.paymentService = paymentService;
        this.userService = userService;
        this.receiptMapper = receiptMapper;
    }

    public Payment findPayment(PaymentProcessor paymentProcessor, String paymentId) {
        try {
            return paymentProcessor.findById(paymentId);
        } catch (ApiException e) {
            log.error("Ошибка поиска платежа {}: {}", paymentId, e.getMessage());
            throw new RuntimeException("Платеж не найден", e);
        }
    }

    public Payment createYooKassaPayment(PaymentProcessor paymentProcessor, String idempotencyKey,String summa,Long orderId) {
        try {
            Amount amount = Amount.builder()
                    .value(summa)
                    .currency(Currency.RUB)
                    .build();

            Confirmation confirmation = Confirmation.builder()
                    .type(Confirmation.Type.REDIRECT)
                    .returnUrl(RETURN_URL)
                    .build();

            Payment payment = Payment.builder()
                    .amount(amount)
                    .description("Оплата заказа в Kortex")
                    .confirmation(confirmation)
                    .metadata(Map.of("orderId", orderId.toString()))
                    .capture(true)
                    .build();

            return paymentProcessor.create(payment, idempotencyKey);
        } catch (Exception e) {
            log.error("Не удалось создать платеж yookassa,ex={}", e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public Receipt createYooKassaReceipt(ReceiptProcessor receiptProcessor, String paymentId) {
        try {
            Payment payment = paymentService.findPayment(paymentId);
            if (!"succeeded".equals(payment.getStatus())) {
                log.warn("Чек создается для неуспешного платежа: {}", paymentId);
            }

            Customer customer = Customer.builder()
                    .email(userService.getCurrentUser().getEmail())
                    .build();

            Amount paymentAmount = payment.getAmount();
            Amount itemAmount = Amount.builder()
                    .value(paymentAmount.getValue())
                    .currency(paymentAmount.getCurrency())
                    .build();

            Item item = Item.builder()
                    .description("Оплата заказа Kortex")
                    .amount(itemAmount)
                    .vatCode(1)
                    .quantity("1")
                    .build();

            Settlement settlement = Settlement.builder()
                    .type(getSettlementType(payment))
                    .amount(itemAmount)
                    .build();

            Receipt receipt = Receipt.builder()
                    .type(Receipt.Type.PAYMENT)
                    .paymentId(paymentId)
                    .customer(customer)
                    .items(List.of(item))
                    .settlements(List.of(settlement))
                    .send(true)
                    .build();

            return receiptProcessor.create(receipt, null);
        } catch (Exception e) {
            log.error("Не удалось создать чек через yookassa,ex={}", e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public ReceiptResponse findReceiptDTO(ReceiptProcessor receiptProcessor, String paymentId) {
        try {
            PaymentEntity payment = paymentService.findByPaymentId(paymentId);

            if (payment.getReceiptId() == null) {
                throw new RuntimeException("Чек ещё не создан для платежа: " + paymentId);
            }

            Receipt receipt = receiptProcessor.findById(payment.getReceiptId());
            return receiptMapper.convertReceiptToReceiptResponse(receipt);
        } catch (Exception e) {
            log.error("Ошибка поиска чека,ex={}", e.getMessage());
            throw new RuntimeException("Чек не найден", e);
        }
    }

    private String getSettlementType(Payment payment) {
        return switch (payment.getPaymentMethod().getType()) {
            case "bank_card", "sberbank", "tinkoff_bank", "alpha_bank", "sbp" ->
                    Settlement.Type.CASHLESS;
            case "yoo_money", "qiwi" ->
                    Settlement.Type.PREPAYMENT;
            case "cash" ->
                    Settlement.Type.PAYOUT;
            case "bank_transfer" ->
                    Settlement.Type.POSTPAYMENT;
            default ->
                    Settlement.Type.CASHLESS;
        };
    }
}

