package by.egrius.pizzaShop.dto.user;

public record UserCreateDto (

        String fullName,
        String email,
        String phone,
        String rawPassword
) { }
