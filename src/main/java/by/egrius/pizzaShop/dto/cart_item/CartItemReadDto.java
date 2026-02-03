package by.egrius.pizzaShop.dto.cart_item;

import by.egrius.pizzaShop.dto.pizza.PizzaReadDto;
import by.egrius.pizzaShop.dto.pizza_size.PizzaSizeReadDto;
import by.egrius.pizzaShop.dto.user.UserReadDto;

import java.time.LocalDateTime;

public record CartItemReadDto(
        Long id,
        UserReadDto userReadDto,
        PizzaReadDto pizzaReadDto,
        PizzaSizeReadDto pizzaSizeReadDto,
        Integer quantity,
        LocalDateTime addedAt
) { }
