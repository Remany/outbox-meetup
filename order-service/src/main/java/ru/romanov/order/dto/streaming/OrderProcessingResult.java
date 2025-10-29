package ru.romanov.order.dto.streaming;

import lombok.Builder;
import ru.romanov.order.domain.entity.OrderItemEntity;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record OrderProcessingResult(BigDecimal totalAmount, List<OrderItemEntity> orderItems) {
}
