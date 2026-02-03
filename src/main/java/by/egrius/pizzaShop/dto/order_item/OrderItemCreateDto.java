package by.egrius.pizzaShop.dto.order_item;

public record OrderItemCreateDto (
        Long pizzaId,
        Long pizzaSizeId,
        Integer quantity
) {}
