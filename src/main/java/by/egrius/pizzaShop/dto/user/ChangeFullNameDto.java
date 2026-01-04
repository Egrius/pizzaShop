package by.egrius.pizzaShop.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangeFullNameDto (
        @NotBlank(message = "Имя не может быть пустым")
        @Size(min = 2, max = 100, message = "Имя должно быть от 2 до 100 символов")
        String newFullName
) {
}
