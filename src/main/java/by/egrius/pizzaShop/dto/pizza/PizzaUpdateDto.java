package by.egrius.pizzaShop.dto.pizza;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.URL;

public record PizzaUpdateDto (

        String name,

        String description,

        String imageUrl,

        String category,

        Boolean available,

        Integer cookingTimeMinutes
) { }
