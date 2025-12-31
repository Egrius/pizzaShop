package by.egrius.pizzaShop.dto;

public record ChangePasswordDto (
        String currentPassword,
        String newRawPassword
){ }
