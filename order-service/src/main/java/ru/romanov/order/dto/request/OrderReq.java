package ru.romanov.order.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.List;

@Builder
public record OrderReq(
        @NotBlank(message = "Email клиента обязателен")
        @Email(message = "Некорректный формат email")
        String customerEmail,

        @NotNull(message = "Список товаров не может быть пустым")
        @Size(
                min = 1,
                message = "Заказ должен содержать хотя бы один товар")
        List<OrderItemReq> items) {
}
