package ru.romanov.order.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.romanov.order.dto.request.OrderItemReq;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventoryService {

    public void reserveItems(UUID orderId, List<OrderItemReq> items) {
        // Логика резервирования товаров на складе
        items.forEach(item -> {
            // Проверка и резервирование каждого товара
            System.out.println("Reserving item: " + item.getProductId() + " for order: " + orderId);
        });
    }
}
