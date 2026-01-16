package by.egrius.pizzaShop.dto.pizza_size;

import by.egrius.pizzaShop.dto.size_template.SizeTemplateInfoDto;
import by.egrius.pizzaShop.entity.PizzaSizeEnum;

import java.math.BigDecimal;

public record PizzaSizeInfoDto (
        SizeTemplateInfoDto sizeTemplateInfoDto,
        BigDecimal price
) { }
