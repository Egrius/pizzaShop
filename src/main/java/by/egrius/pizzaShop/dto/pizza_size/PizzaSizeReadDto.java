package by.egrius.pizzaShop.dto.pizza_size;

import by.egrius.pizzaShop.dto.size_template.SizeTemplateReadDto;

import java.math.BigDecimal;

public record PizzaSizeReadDto (
        Long id,
        SizeTemplateReadDto sizeTemplateReadDto,
        BigDecimal price,
        boolean available
) { }
