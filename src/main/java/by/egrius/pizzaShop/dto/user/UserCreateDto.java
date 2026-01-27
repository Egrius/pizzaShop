package by.egrius.pizzaShop.dto.user;

import jakarta.validation.constraints.*;

public record UserCreateDto (
        @NotNull
        @NotBlank(message = "Полное имя не может быть пустым")
        @Size(min = 2, max = 100, message = "Полное имя должно быть от 2 до 100 символов")
        String fullName,

        @NotNull
        @NotBlank(message = "Email не может быть пустым")
        @Email(message = "Некорректный формат email")
        String email,

        @NotNull
        @NotBlank(message = "Пароль не может быть пустым")
        @Size(min = 5, max = 100, message = "Пароль должен быть от 5 до 100 символов")
        @Pattern(
                regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).*$",
                message = "Пароль должен содержать хотя бы одну цифру, одну строчную и одну заглавную букву"
        )
        String rawPassword,

        @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Некорректный формат телефона")
        String phone
) { }
