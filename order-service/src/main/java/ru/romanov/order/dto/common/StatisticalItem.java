package ru.romanov.order.dto.common;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class StatisticalItem {
    private String productId;
    private String productName;
    private String productCategoryCode;
    private Integer quantity;
    private BigDecimal value;
    private String measurementUnit;
}
