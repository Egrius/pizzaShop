package by.egrius.pizzaShop.filter;

import java.math.BigDecimal;

public record PizzaFilter (
        String name,
        String description,
        String category,
        BigDecimal startPrice,
        BigDecimal fromPrice,
        BigDecimal toPrice
) { }
