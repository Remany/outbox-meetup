package ru.romanov.order.dto.streaming;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class CbrPaymentEvent {
    private UUID orderId;
    private BigDecimal amount;
    private String currency;
    private UUID customerId;
    private LocalDateTime timestamp;
    private boolean isSuspicious;
    private String riskLevel;
}
