package by.egrius.pizzaShop.payment_imitation;

import java.time.LocalDateTime;

public record PaymentResponseDto(
        boolean success,
        String transactionId,  // null если неудача
        String errorCode,      // null если успех
        String message,
        LocalDateTime timestamp
) {
    public static PaymentResponseDto success(String transactionId, String message) {
        return new PaymentResponseDto(true, transactionId, null, message, LocalDateTime.now());
    }

    public static PaymentResponseDto failed(String errorCode, String message) {
        return new PaymentResponseDto(false, null, errorCode, message, LocalDateTime.now());
    }
}