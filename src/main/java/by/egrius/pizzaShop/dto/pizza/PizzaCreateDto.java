package by.egrius.pizzaShop.dto.pizza;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.URL;

import java.util.Map;
import java.util.Set;

public record PizzaCreateDto (
        @NotNull
        @Length(min = 5, max = 100)
        String name,

        @NotNull
        @Length(max = 100)
        String description,

        @NotNull
        @URL
        String imageUrl,

        @NotNull
        String category,

        boolean available,

        @NotNull
        @Positive
        @Min(5)
        Integer cookingTimeMinutes,

        @NotNull
        @NotEmpty
        Map<Long, Integer> ingredientWeights,

        @NotNull
        @NotEmpty
        Set<Long> sizeTemplateIds
) { }
