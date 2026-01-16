package by.egrius.pizzaShop.dto.pizza;

import java.math.BigDecimal;

public record PizzaCardDto (
        Long id,
        String name,
        String description,
        String imageUrl,
        String category,
        BigDecimal startPrice
){ }
