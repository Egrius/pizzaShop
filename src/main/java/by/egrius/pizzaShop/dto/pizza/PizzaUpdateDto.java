package by.egrius.pizzaShop.dto.pizza;

public record PizzaUpdateDto (
        String name,
        String description,
        String imageUrl,
        String category,
        Boolean available,
        Integer cookingTimeMinutes
) { }
