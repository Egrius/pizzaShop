package by.egrius.pizzaShop.dto.ingredient;

import java.math.BigDecimal;

public record IngredientUpdateDto (
        String newName,
        String newDescription,
        BigDecimal newPrice,
        Boolean available
) { }
