package by.egrius.pizzaShop.dto.order_item;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderItemReadDto (
        Long id,
        Long orderId,
        Long pizzaId,
        Long pizzaSizeId,
        Integer quantity,
        BigDecimal unitPrice,
        LocalDateTime addedAt,
        BigDecimal subTotal
) { }