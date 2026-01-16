package by.egrius.pizzaShop.dto.pizza;

import by.egrius.pizzaShop.dto.ingredient.IngredientInfoDto;
import by.egrius.pizzaShop.dto.pizza_size.PizzaSizeInfoDto;
import java.util.List;

public record PizzaCardDetailsDto(
        Long id,
        String name,
        String description,
        String imageUrl,
        String category,
        Integer cookingTimeMinutes,
        List<IngredientInfoDto> ingredientInfoDtos,
        List<PizzaSizeInfoDto> pizzaSizeInfoDtos
) { }