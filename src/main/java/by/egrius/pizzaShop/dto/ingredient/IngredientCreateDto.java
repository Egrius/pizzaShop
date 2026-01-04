package by.egrius.pizzaShop.dto.ingredient;

import java.math.BigDecimal;

public record IngredientCreateDto (
        String name,
        String description,
        BigDecimal price,
        boolean available
) { }
