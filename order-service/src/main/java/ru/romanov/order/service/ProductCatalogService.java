package ru.romanov.order.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.romanov.order.dto.common.ProductInfo;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ProductCatalogService {

    public ProductInfo getProductInfo(String productId) {
        // Заглушка - в реальности запрос к каталогу товаров
        return ProductInfo.builder()
                .id(productId)
                .name("Product " + productId)
                .price(BigDecimal.valueOf(1000))
                .availableQuantity(100)
                .category("ELECTRONICS")
                .manufacturer("Test Manufacturer")
                .build();
    }
}
