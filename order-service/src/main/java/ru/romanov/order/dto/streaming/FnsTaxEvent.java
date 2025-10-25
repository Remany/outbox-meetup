package ru.romanov.order.dto.streaming;

import lombok.Builder;
import lombok.Data;
import ru.romanov.order.dto.common.TaxItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class FnsTaxEvent {
    private UUID orderId;
    private String customerTin;
    private BigDecimal totalAmount;
    private BigDecimal taxAmount;
    private LocalDateTime transactionDate;
    private List<TaxItem> items;
}
