package ru.romanov.order.dto.response;

import lombok.Builder;
import lombok.Data;
import ru.romanov.order.domain.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class OrderDetailResponse {
    private UUID orderId;
    private OrderStatus status;
    private String customerEmail;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private List<OrderItemDetail> items;
}
