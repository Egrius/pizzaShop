package by.egrius.pizzaShop.dto.error;

import java.time.LocalDateTime;

public record ExceptionDto (
        String message,
        int code,
        String path,
        LocalDateTime dateTime
){}
