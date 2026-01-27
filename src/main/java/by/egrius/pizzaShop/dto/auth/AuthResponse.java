package by.egrius.pizzaShop.dto.auth;

public record AuthResponse(
         Long id,
         String email,
         String fullName,
         String role,
         String jwtToken,
         String message
) {}
