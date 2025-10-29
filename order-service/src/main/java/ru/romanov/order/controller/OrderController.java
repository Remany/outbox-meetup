package ru.romanov.order.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.romanov.order.domain.entity.OrderEntity;
import ru.romanov.order.domain.enums.OrderStatus;
import ru.romanov.order.dto.request.OrderReq;
import ru.romanov.order.dto.response.OrderDetailResponse;
import ru.romanov.order.dto.response.OrderItemDetail;
import ru.romanov.order.dto.response.OrderResponse;
import ru.romanov.order.dto.response.OrderSummaryResponse;
import ru.romanov.order.service.OrderService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/order")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderReq request) {
        log.info("Received request to create order for customer: {}", request.customerEmail());

        OrderEntity order = orderService.createOrder(request);

        OrderResponse response = OrderResponse.builder()
                .orderId(order.getId())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .customerEmail(order.getCustomerEmail())
                .createdAt(order.getCreatedAt())
                .message("Заказ успешно создан и отправлен на обработку")
                .build();

        log.info("Order created successfully: {}", order.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDetailResponse> getOrder(@PathVariable UUID orderId) {
        log.info("Fetching order details for: {}", orderId);

        // В реальности здесь был бы вызов сервиса для получения заказа
        // OrderEntity order = orderService.getOrder(orderId);

        // Заглушка для демонстрации
        OrderDetailResponse response = OrderDetailResponse.builder()
                .orderId(orderId)
                .status(OrderStatus.CONFIRMED)
                .customerEmail("customer@example.com")
                .totalAmount(BigDecimal.valueOf(15000))
                .createdAt(LocalDateTime.now())
                .items(List.of(
                        OrderItemDetail.builder()
                                .productId("prod-123")
                                .productName("Ноутбук игровой")
                                .quantity(1)
                                .price(BigDecimal.valueOf(15000))
                                .build()
                ))
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<OrderSummaryResponse>> getOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Fetching orders list - page: {}, size: {}", page, size);

        // Заглушка для демонстрации
        List<OrderSummaryResponse> orders = List.of(
                OrderSummaryResponse.builder()
                        .orderId(UUID.randomUUID())
                        .status(OrderStatus.CONFIRMED)
                        .totalAmount(BigDecimal.valueOf(15000))
                        .customerEmail("customer1@example.com")
                        .createdAt(LocalDateTime.now().minusHours(2))
                        .build(),
                OrderSummaryResponse.builder()
                        .orderId(UUID.randomUUID())
                        .status(OrderStatus.PAID)
                        .totalAmount(BigDecimal.valueOf(7500))
                        .customerEmail("customer2@example.com")
                        .createdAt(LocalDateTime.now().minusDays(1))
                        .build()
        );

        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable UUID orderId) {
        log.info("Cancelling order: {}", orderId);

        // В реальности: orderService.cancelOrder(orderId);

        OrderResponse response = OrderResponse.builder()
                .orderId(orderId)
                .status(OrderStatus.CANCELLED)
                .message("Заказ успешно отменен")
                .build();

        return ResponseEntity.ok(response);
    }
}
