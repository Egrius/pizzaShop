package by.egrius.pizzaShop.dto.size_template;

import by.egrius.pizzaShop.entity.PizzaSizeEnum;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record SizeTemplateCreateDto (
        @NotNull
        @Max(value = 20)
        PizzaSizeEnum sizeName,

        @NotNull
        @Max(value = 50)
        String displayName,

        @NotNull
        @Positive
        Integer diameterCm,

        @NotNull
        @Positive
        Integer weightGrams,

        @NotNull
        @Positive
        BigDecimal sizeMultiplier
) {}
