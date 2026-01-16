package by.egrius.pizzaShop.dto.pizza;

import by.egrius.pizzaShop.dto.ingredient.IngredientWeightDto;
import by.egrius.pizzaShop.entity.PizzaSizeEnum;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record PizzaCreateDto (
        String name,
        String description,
        String imageUrl,
        String category,
        boolean available,
        Integer cookingTimeMinutes,
        Map<Long, Integer> ingredientWeights ,
        Set<Long> sizeTemplateIds
) { }
