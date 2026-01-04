package by.egrius.pizzaShop.dto.pizza;

import java.util.Set;

public record PizzaUpdateDto (
        String name,
        String description,
        String imageUrl,
        String category,
        Boolean available,
        Integer cookingTimeMinutes
) { }
