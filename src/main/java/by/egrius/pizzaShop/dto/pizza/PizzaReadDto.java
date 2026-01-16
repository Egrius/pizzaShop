package by.egrius.pizzaShop.dto.pizza;

import by.egrius.pizzaShop.dto.pizza_ingredient.PizzaIngredientReadDto;
import by.egrius.pizzaShop.dto.pizza_size.PizzaSizeReadDto;

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
        List<PizzaIngredientReadDto> pizzaIngredientReadDtos,
        List<PizzaSizeReadDto> pizzaSizeReadDtos
) { }
