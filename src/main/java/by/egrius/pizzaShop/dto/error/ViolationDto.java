package by.egrius.pizzaShop.dto.error;

public record ViolationDto (
        String field,
        String msg
) { }
