package by.egrius.pizzaShop.dto;

import by.egrius.pizzaShop.entity.UserRole;

import java.time.LocalDateTime;

public record UserReadDto (
        Long id,
        String fullName,
        String email,
        String phone,
        UserRole role,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) { }
