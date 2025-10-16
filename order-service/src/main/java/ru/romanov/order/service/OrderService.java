package ru.romanov.order.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.romanov.order.domain.entity.OrderEntity;
import ru.romanov.order.dto.OrderReq;
import ru.romanov.outbox.facade.StreamingFacade;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final StreamingFacade streamingFacade;

    @Transactional
    public OrderEntity createOrder(OrderReq req) {
        OrderEntity entity = new OrderEntity();
        streamingFacade.initDataProcessing(entity.getId().toString(), entity, Map.of("ppkc", "ppkc"));
        return null;
    }
}
