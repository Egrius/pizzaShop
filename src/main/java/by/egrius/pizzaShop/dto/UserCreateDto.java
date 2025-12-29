package by.egrius.pizzaShop.dto;

public record UserCreateDto (

        String fullName,
        String email,
        String phone,
        String rawPassword
) { }
