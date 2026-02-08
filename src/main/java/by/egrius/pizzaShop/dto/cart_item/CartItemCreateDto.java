package by.egrius.pizzaShop.dto.cart_item;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CartItemCreateDto (

        @NotNull
        @Positive
        Long pizzaId,

        @NotNull
        @Positive
        Long pizzaSizeId,

        @NotNull
        @Positive
        @Min(1) @Max(100)
        Integer quantity
) {}
