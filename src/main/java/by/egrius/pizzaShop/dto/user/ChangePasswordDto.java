package by.egrius.pizzaShop.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordDto(
        @NotBlank(message = "Текущий пароль не может быть пустым")
        String currentPassword,

        @NotBlank(message = "Новый пароль не может быть пустым")
        @Size(min = 8, message = "Пароль должен содержать минимум 8 символов")
        String newPassword,

        @NotBlank(message = "Подтверждение пароля не может быть пустым")
        String confirmPassword
) {
    public ChangePasswordDto {
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("Пароли не совпадают");
        }
    }
}
