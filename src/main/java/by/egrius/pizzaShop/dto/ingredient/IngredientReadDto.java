package by.egrius.pizzaShop.dto.ingredient;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record IngredientReadDto (
        Long id,
        String name,
        String description,
        BigDecimal price,
        boolean available,
        LocalDateTime createdAt,
        Long version
) { }
