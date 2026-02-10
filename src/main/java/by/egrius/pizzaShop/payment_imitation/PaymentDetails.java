package by.egrius.pizzaShop.payment_imitation;

import java.math.BigDecimal;

public record PaymentDetails(
        String cardNumber,      // "4111111111111111"
        String cardHolder,      // "IVAN IVANOV"
        String expiryDate,      // "12/25"
        String cvv            // "123"
) {}