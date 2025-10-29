package ru.romanov.order.dto.common;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ConsumerItem {
    private String productId;
    private String productName;
    private String manufacturer;
    private Integer quantity;
    private BigDecimal price;
    private String warrantyPeriod;
    private String safetyCertification;
}
