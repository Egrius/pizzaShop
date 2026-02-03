package by.egrius.pizzaShop.filter;

import java.math.BigDecimal;

public record PizzaFilter (
        String name,
        String category,
        BigDecimal fromPrice,
        BigDecimal toPrice
) { }
