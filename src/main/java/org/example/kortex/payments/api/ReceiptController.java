package org.example.kortex.payments.api;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.example.kortex.payments.api.dto.response.receipt.ReceiptResponse;
import org.example.kortex.payments.domain.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/receipts")
@RequiredArgsConstructor
public class ReceiptController {
    private final PaymentService paymentService;

    @Operation(summary = "Получить информацию о чеке")
    @GetMapping("/{paymentId}")
    public ResponseEntity<ReceiptResponse> getReceipt(
            @PathVariable String paymentId
    ){
        return ResponseEntity.ok(paymentService.findReceipt(paymentId));
    }

    @Operation(summary = "Создать чек")
    @PostMapping("/{paymentId}")
    public ResponseEntity<ReceiptResponse> createReceipt(
            @PathVariable String paymentId
    ){
        return ResponseEntity.ok(paymentService.createReceipt(paymentId));
    }


}
