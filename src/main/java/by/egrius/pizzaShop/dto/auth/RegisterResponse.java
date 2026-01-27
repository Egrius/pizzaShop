package by.egrius.pizzaShop.dto.auth;

public record RegisterResponse(
        Long userId,
        String userEmail,
        String userFullName,
        String message
) {}
