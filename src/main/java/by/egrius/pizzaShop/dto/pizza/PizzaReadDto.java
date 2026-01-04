package by.egrius.pizzaShop.dto.pizza;

import by.egrius.pizzaShop.dto.ingredient.IngredientReadDto;

import java.time.LocalDateTime;
import java.util.List;

public record PizzaReadDto (
        Long id,
        String name,
        String description,
        String imageUrl,
        String category,
        boolean available,
        Integer cookingTimeMinutes,
        LocalDateTime createdAt,
        List<IngredientReadDto> ingredients
) { }
