package by.egrius.pizzaShop.dto.pizza;

import by.egrius.pizzaShop.annotation.AtLeastOneFieldNotNullAndValidPizzaUpdate;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.URL;

@AtLeastOneFieldNotNullAndValidPizzaUpdate
public record PizzaUpdateDto (

        @Length(min = 5, max = 100)
        String name,

        @Length(max = 100)
        String description,

        @URL
        @Length(max = 100)
        String imageUrl,

        String category,

        Boolean available,

        @Positive
        @Min(5)
        Integer cookingTimeMinutes
) { }