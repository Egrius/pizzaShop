package by.egrius.pizzaShop.dto.cart_item;

public record CartItemCreateDto (
    Long userId,
    Long pizzaId,
    Long pizzaSizeId,
    Integer quantity
) {}
