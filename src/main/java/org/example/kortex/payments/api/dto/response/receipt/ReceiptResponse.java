package org.example.kortex.payments.api.dto.response.receipt;

import java.util.List;

public record ReceiptResponse(
        String id,
        String type,
        String paymentId,
        String status,
        String amount,

        String fiscalDocumentNumber,
        String fiscalStorageNumber,
        String fiscalAttribute,
        String registeredAt,
        String fiscalProviderId,

        List<ReceiptItem> items,
        List<SettlementReceipt> settlements,

        String sellerName
) {
}
