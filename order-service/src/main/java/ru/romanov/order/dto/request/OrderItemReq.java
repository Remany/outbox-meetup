package ru.romanov.order.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderItemReq {
    @NotBlank(message = "ID товара обязателен")
    private String productId;

    @NotBlank(message = "Название товара обязательно")
    private String productName;

    @NotNull(message = "Количество обязательно")
    @Min(
            value = 1,
            message = "Количество должно быть не менее 1")
    private Integer quantity;

    @NotNull(message = "Цена обязательна")
    @DecimalMin(
            value = "0.0",
            inclusive = false,
            message = "Цена должна быть больше 0")
    private BigDecimal price;
}
