package by.egrius.pizzaShop.dto.ingredient;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record IngredientIdsRequest(
        @NotEmpty(message = "Список ID не может быть пустым")
        @Size(min = 1, max = 100, message = "Можно запросить от 1 до 100 ингредиентов")
        Set<Long> ids
) {}