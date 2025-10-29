package ru.romanov.order.dto.response;

import lombok.Builder;
import lombok.Data;
import ru.romanov.order.domain.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class OrderResponse {
    private UUID orderId;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String customerEmail;
    private LocalDateTime createdAt;
    private String message;
}
