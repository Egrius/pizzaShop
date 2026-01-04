package by.egrius.pizzaShop.dto.pizza;

import by.egrius.pizzaShop.dto.ingredient.IngredientCreateDto;

import java.util.List;
import java.util.Set;

public record PizzaCreateDto (
        String name,
        String description,
        String imageUrl,
        String category,
        boolean available,
        Integer cookingTimeMinutes,
        Set<Long> ingredientIds
) { }
