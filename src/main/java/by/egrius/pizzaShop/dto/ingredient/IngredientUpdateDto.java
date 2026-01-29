package by.egrius.pizzaShop.dto.ingredient;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;

public record IngredientUpdateDto(

        @NotBlank(message = "Название не может быть пустым")
        @Length(min = 1, max = 100, message = "Название должно быть от 1 до 100 символов")
        String name,

        @NotBlank(message = "Описание не может быть пустым")
        @Length(min = 1, max = 500, message = "Описание должно быть от 1 до 500 символов")
        String description,

        @NotNull(message = "Цена обязательна")
        @Positive(message = "Цена должна быть положительной")
        @Digits(integer = 6, fraction = 2, message = "Некорректный формат цены")
        BigDecimal price,

        @NotNull(message = "Статус доступности обязателен")
        Boolean available

) {}