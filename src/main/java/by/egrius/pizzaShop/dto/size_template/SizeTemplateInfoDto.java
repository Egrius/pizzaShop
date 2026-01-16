package by.egrius.pizzaShop.dto.size_template;

import by.egrius.pizzaShop.entity.PizzaSizeEnum;

public record SizeTemplateInfoDto (
        PizzaSizeEnum sizeName,
        String displayName,
        Integer diameterCm,
        Integer weightGrams
) { }