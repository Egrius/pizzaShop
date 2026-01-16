package by.egrius.pizzaShop.dto.pizza;

import by.egrius.pizzaShop.repository.PizzaRepository;

import java.time.LocalDateTime;

public record PizzaUpdateResponseDto(
        Long id,
        String name,
        String description,
        String imageUrl,
        String category,
        boolean isAvailable,
        Integer cookingTimeMinutes,
        LocalDateTime updatedAt
) { }