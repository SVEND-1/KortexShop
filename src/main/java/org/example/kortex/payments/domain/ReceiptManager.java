package org.example.kortex.payments.domain;

import lombok.extern.slf4j.Slf4j;
import org.example.kortex.payments.db.PaymentEntity;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import ru.loolzaaa.youkassa.model.Receipt;

@Slf4j
@Component
public class ReceiptManager {

    private final PaymentService paymentService;

    public ReceiptManager(@Lazy PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    public void saveReceipt(String paymentId, Receipt saved) {
        try {
            PaymentEntity paymentEntity = paymentService.findByPaymentId(paymentId);
            paymentEntity.setReceiptId(saved.getId());
            paymentService.save(paymentEntity);
        }catch (Exception e) {
            log.error("Не удалось сохранить чек id={},ex={}",saved.getId(),e.getMessage());
        }
    }


}
