package ru.romanov.order.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.romanov.order.domain.entity.CustomerEntity;
import ru.romanov.order.domain.entity.OrderEntity;
import ru.romanov.order.domain.entity.OrderItemEntity;
import ru.romanov.order.domain.enums.OrderStatus;
import ru.romanov.order.domain.repository.OrderRepository;
import ru.romanov.order.dto.request.OrderItemReq;
import ru.romanov.order.dto.request.OrderReq;
import ru.romanov.order.dto.streaming.CbrPaymentEvent;
import ru.romanov.order.dto.common.ConsumerItem;
import ru.romanov.order.dto.streaming.FnsTaxEvent;
import ru.romanov.order.dto.streaming.OrderProcessingResult;
import ru.romanov.order.dto.common.ProductInfo;
import ru.romanov.order.dto.streaming.RosstatEconomicEvent;
import ru.romanov.order.dto.streaming.RpnConsumerEvent;
import ru.romanov.order.dto.common.StatisticalItem;
import ru.romanov.order.dto.common.TaxItem;
import ru.romanov.order.exception.InsufficientInventoryException;
import ru.romanov.outbox.facade.StreamingFacade;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final StreamingFacade streamingFacade;
    private final OrderRepository orderRepository;
    private final ProductCatalogService productCatalogService;
    private final CustomerService customerService;
    private final InventoryService inventoryService;

    @Transactional
    public OrderEntity createOrder(OrderReq req) {
        // 1. Валидация клиента
        CustomerEntity customer = customerService.validateCustomer(req.customerEmail());

        // 2. Проверка наличия товаров и расчет общей суммы
        OrderProcessingResult processingResult = processOrderItems(req.items());

        // 3. Создание заказа
        OrderEntity order = buildOrderEntity(req, customer, processingResult);

        // 4. Сохранение заказа
        OrderEntity savedOrder = orderRepository.save(order);

        // 5. Резервирование товаров на складе
        inventoryService.reserveItems(savedOrder.getId(), req.items());

        // 6. Отправка событий в разные системы через transactional outbox
        sendOrderEvents(savedOrder, processingResult);

        return savedOrder;
    }

    private OrderProcessingResult processOrderItems(List<OrderItemReq> items) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItemEntity> orderItems = new ArrayList<>();

        for (OrderItemReq itemReq : items) {
            // Проверка существования товара
            ProductInfo product = productCatalogService.getProductInfo(itemReq.getProductId());

            // Проверка доступного количества
            if (product.getAvailableQuantity() < itemReq.getQuantity()) {
                throw new InsufficientInventoryException(
                        "Недостаточно товара: " + product.getName() +
                                ". Доступно: " + product.getAvailableQuantity()
                );
            }

            // Расчет стоимости позиции
            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);

            // Создание entity позиции заказа
            OrderItemEntity orderItem = new OrderItemEntity();
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setQuantity(itemReq.getQuantity());
            orderItem.setPrice(product.getPrice());
            orderItems.add(orderItem);
        }

        return new OrderProcessingResult(totalAmount, orderItems);
    }

    private OrderEntity buildOrderEntity(OrderReq req, CustomerEntity customer, OrderProcessingResult result) {
        OrderEntity order = new OrderEntity();
        order.setCustomerEmail(customer.getEmail());
        order.setCustomerId(customer.getId());
        order.setTotalAmount(result.totalAmount());
        order.setStatus(OrderStatus.CONFIRMED);
        order.setItems(result.orderItems());

        // Устанавливаем связь для каскадного сохранения
        result.orderItems().forEach(item -> item.setOrder(order));

        return order;
    }

    private void sendOrderEvents(OrderEntity order, OrderProcessingResult processingResult) {
        // 1. ФНС - Федеральная Налоговая Служба (для налогового учета)
        streamingFacade.initDataProcessing(
                order.getId().toString(),
                buildFnsEvent(order, processingResult),
                Map.of("common-system", "tax-events-topic")
        );

        // 2. ЦБР - Центральный Банк России (для мониторинга платежей)
        streamingFacade.initDataProcessing(
                order.getId().toString(),
                buildCbrEvent(order),
                Map.of("common-system", "payment-monitoring-topic")
        );

        // 3. РПН - Роспотребнадзор (для защиты прав потребителей)
        streamingFacade.initDataProcessing(
                order.getId().toString(),
                buildRpnEvent(order, processingResult),
                Map.of("common-system", "consumer-protection-topic")
        );

        // 4. Росстат - Федеральная Служба Государственной Статистики
        streamingFacade.initDataProcessing(
                order.getId().toString(),
                buildRosstatEvent(order, processingResult),
                Map.of("common-system", "statistics-topic")
        );
    }

    private FnsTaxEvent buildFnsEvent(OrderEntity order, OrderProcessingResult result) {
        return FnsTaxEvent.builder()
                .orderId(order.getId())
                .customerTin(order.getCustomerTin()) // ИНН клиента
                .totalAmount(order.getTotalAmount())
                .taxAmount(order.getTotalAmount().multiply(BigDecimal.valueOf(0.2))) // НДС 20%
                .transactionDate(LocalDateTime.now())
                .items(result.orderItems().stream()
                        .map(this::toTaxItem)
                        .collect(Collectors.toList()))
                .build();
    }

    private CbrPaymentEvent buildCbrEvent(OrderEntity order) {
        return CbrPaymentEvent.builder()
                .orderId(order.getId())
                .amount(order.getTotalAmount())
                .currency("RUB")
                .customerId(order.getCustomerId())
                .timestamp(LocalDateTime.now())
                .isSuspicious(order.getTotalAmount().compareTo(BigDecimal.valueOf(1000000)) > 0) // > 1 млн
                .build();
    }

    private RpnConsumerEvent buildRpnEvent(OrderEntity order, OrderProcessingResult result) {
        return RpnConsumerEvent.builder()
                .orderId(order.getId())
                .customerEmail(order.getCustomerEmail())
                .purchaseDate(LocalDateTime.now())
                .items(result.orderItems().stream()
                        .map(this::toConsumerItem)
                        .collect(Collectors.toList()))
                .totalAmount(order.getTotalAmount())
                .warrantyInfo("STANDARD_2_YEARS")
                .build();
    }

    private TaxItem toTaxItem(OrderItemEntity item) {
        return TaxItem.builder()
                .productId(item.getProductId())
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .unitPrice(item.getPrice())
                .totalPrice(item.getTotalPrice())
                .vatRate("20%")
                .build();
    }

    private ConsumerItem toConsumerItem(OrderItemEntity item) {
        return ConsumerItem.builder()
                .productId(item.getProductId())
                .productName(item.getProductName())
                .manufacturer("Производитель " + item.getProductId())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .warrantyPeriod("24 месяца")
                .safetyCertification("РОСТЕСТ")
                .build();
    }

    private RosstatEconomicEvent buildRosstatEvent(OrderEntity order, OrderProcessingResult result) {
        return RosstatEconomicEvent.builder()
                .orderId(order.getId())
                .regionCode("77") // Москва
                .economicActivityType("RETAIL_TRADE")
                .transactionValue(order.getTotalAmount())
                .reportingDate(LocalDateTime.now())
                .items(result.orderItems().stream()
                        .map(item -> StatisticalItem.builder()
                                .productId(item.getProductId())
                                .productName(item.getProductName())
                                .productCategoryCode("47.61.1")
                                .quantity(item.getQuantity())
                                .value(item.getTotalPrice())
                                .measurementUnit("штука")
                                .build())
                        .collect(Collectors.toList()))
                .statisticalCategory("RETAIL_SALES_INDEX")
                .build();
    }
}
