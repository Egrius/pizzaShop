package by.egrius.pizzaShop.dto.pizza_ingredient;

import by.egrius.pizzaShop.dto.ingredient.IngredientCreateDto;

public record PizzaIngredientCreateDto (
    Long pizzaId,
    IngredientCreateDto ingredientCreateDto,
    Integer weightGrams
){}
