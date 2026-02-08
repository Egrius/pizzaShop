package by.egrius.pizzaShop.dto.cart;

import by.egrius.pizzaShop.dto.cart_item.CartItemReadDto;

import java.math.BigDecimal;
import java.util.List;

public record CartReadDto(
        List<CartItemReadDto> cartItemReadDtos,
        BigDecimal totalPrice
) { }
