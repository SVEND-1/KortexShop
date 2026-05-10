package org.example.kortex.payments.domain.mapper;

import org.example.kortex.payments.api.dto.response.receipt.ReceiptItem;
import org.example.kortex.payments.api.dto.response.receipt.ReceiptResponse;
import org.example.kortex.payments.api.dto.response.receipt.SettlementReceipt;
import org.mapstruct.Mapper;
import ru.loolzaaa.youkassa.model.Receipt;
import ru.loolzaaa.youkassa.pojo.Item;
import ru.loolzaaa.youkassa.pojo.Settlement;

@Mapper(componentModel = "spring")
public interface ReceiptMapper {

    default ReceiptResponse convertReceiptToReceiptResponse(Receipt receipt) {
        return new ReceiptResponse(
                receipt.getId(),
                receipt.getType(),
                receipt.getPaymentId(),
                receipt.getStatus(),
                "999",

                receipt.getFiscalDocumentNumber(),
                receipt.getFiscalStorageNumber(),
                receipt.getFiscalAttribute(),
                receipt.getRegisteredAt(),
                receipt.getFiscalProviderId(),

                receipt.getItems().stream()
                        .map(this::convertToReceiptItem)
                        .toList(),

                receipt.getSettlements().stream()
                        .map(this::convertToSettlementReceipt)
                        .toList(),

                "Tula Hackaton"
        );
    }

    default ReceiptItem convertToReceiptItem(Item item) {
        return new ReceiptItem(
                item.getDescription(),
                item.getQuantity().toString(),
                item.getAmount().getValue(),
                item.getAmount().getCurrency(),
                item.getVatCode()
        );
    }

    default SettlementReceipt convertToSettlementReceipt(Settlement settlement) {
        return new SettlementReceipt(
                settlement.getType(),
                settlement.getAmount().getValue(),
                settlement.getAmount().getCurrency()
        );
    }

}
