package ru.romanov.order.dto.streaming;

import lombok.Builder;
import lombok.Data;
import ru.romanov.order.dto.common.StatisticalItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class RosstatEconomicEvent {
    private UUID orderId;
    private String regionCode;
    private String economicActivityType;
    private BigDecimal transactionValue;
    private LocalDateTime reportingDate;
    private List<StatisticalItem> items;
    private String statisticalCategory;
}
