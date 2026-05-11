package org.example.kortex.payments.api;

import lombok.RequiredArgsConstructor;
import org.example.kortex.orders.api.dto.OrderPaymentApproved;
import org.example.kortex.orders.domain.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    
    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<List<OrderPaymentApproved>> getPayments() {
        return ResponseEntity.ok(orderService.getOrdersPayment());
    }

    @PostMapping("/{id}")
    public ResponseEntity<String> postPayment(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.paymentApprove(id));
    }
}
