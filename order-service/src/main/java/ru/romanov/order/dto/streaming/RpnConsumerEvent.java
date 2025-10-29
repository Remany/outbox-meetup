package ru.romanov.order.dto.streaming;

import lombok.Builder;
import lombok.Data;
import ru.romanov.order.dto.common.ConsumerItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class RpnConsumerEvent {
    private UUID orderId;
    private String customerEmail;
    private LocalDateTime purchaseDate;
    private List<ConsumerItem> items;
    private BigDecimal totalAmount;
    private String warrantyInfo;
    private String consumerRightsInfo;
}
