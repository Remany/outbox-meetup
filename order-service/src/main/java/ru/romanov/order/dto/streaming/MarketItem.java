package ru.romanov.order.dto.streaming;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class MarketItem {
    private String productId;
    private String productName;
    private String supplier;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal marketShareImpact;
}
