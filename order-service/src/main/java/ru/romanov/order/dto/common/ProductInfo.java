package ru.romanov.order.dto.common;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProductInfo {
    private String id;
    private String name;
    private BigDecimal price;
    private Integer availableQuantity;
    private String category;
    private String manufacturer;
}
