package org.example.kortex.payments.domain.mapper;

import org.example.kortex.payments.api.dto.response.payment.PaymentPageResponse;
import org.example.kortex.payments.api.dto.response.payment.PaymentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import ru.loolzaaa.youkassa.model.Payment;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    @Mapping(target = "value", source = "amount.value")
    PaymentResponse convertEntityToPaymentResponse(Payment payment);

    List<PaymentResponse> convertEntityToPaymentResponseList(List<PaymentResponse> payments);

    default PaymentPageResponse toPageResponse(Page<PaymentResponse> paymentResponses) {
        if (paymentResponses == null) {
            return null;
        }

        return new PaymentPageResponse(
                convertEntityToPaymentResponseList(paymentResponses.getContent()),
                paymentResponses.getNumber(),
                paymentResponses.getSize(),
                paymentResponses.getTotalElements(),
                paymentResponses.getTotalPages(),
                paymentResponses.isFirst(),
                paymentResponses.isLast(),
                paymentResponses.isEmpty()
        );
    }
}
