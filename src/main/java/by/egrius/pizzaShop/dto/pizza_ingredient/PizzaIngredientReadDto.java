package by.egrius.pizzaShop.dto.pizza_ingredient;

import by.egrius.pizzaShop.dto.ingredient.IngredientReadDto;

public record PizzaIngredientReadDto (
        Long id,
        IngredientReadDto ingredientReadDto,
        Integer weightGrams
) { }
