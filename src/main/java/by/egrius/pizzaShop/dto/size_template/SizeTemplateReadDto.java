package by.egrius.pizzaShop.dto.size_template;

import by.egrius.pizzaShop.entity.PizzaSizeEnum;

import java.math.BigDecimal;

public record SizeTemplateReadDto (
    Long id,
    PizzaSizeEnum sizeName,
    String displayName,
    Integer diameterCm,
    Integer weightGrams,
    BigDecimal sizeMultiplier
) { }
