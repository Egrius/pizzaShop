package by.egrius.pizzaShop.payment_imitation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class PaymentService {

    public PaymentResponseDto processPaymentSimple(PaymentDetails paymentDetails) {
        log.info("Processing payment for card: {}", paymentDetails.cardNumber());

        // Имитация задержки платежного шлюза
        simulateNetworkDelay(3000, 5000);

        // 80% успешных, 20% неудачных
        boolean isSuccess = Math.random() > 0.2;

        if (isSuccess) {
            String transactionId = "TXN_" + System.currentTimeMillis() + "_" +
                    (int)(Math.random() * 1000);
            return PaymentResponseDto.success(transactionId, "Payment processed successfully");
        } else {
            return PaymentResponseDto.failed("PAYMENT_DECLINED",
                    "Insufficient funds or card declined");
        }
    }

    // Метод с валидацией карты (простая логика)
    @Async("paymentProcessingExecutor")
    public CompletableFuture<PaymentResponseDto> processPaymentWithValidation(PaymentDetails paymentDetails) {

        log.info("=== ASYNC METHOD STARTED in thread: {} ===", Thread.currentThread().getName());

        try {
            // 1. Базовая валидация карты
            if (!isValidCardNumber(paymentDetails.cardNumber())) {
                return CompletableFuture.completedFuture(PaymentResponseDto.failed("INVALID_CARD",
                        "Invalid card number format"));
            }

            if (!isValidExpiryDate(paymentDetails.expiryDate())) {
                return CompletableFuture.completedFuture(PaymentResponseDto.failed("EXPIRED_CARD",
                        "Card has expired"));
            }

            // 2. Проверка CVV
            if (!isValidCvv(paymentDetails.cvv())) {
                return CompletableFuture.completedFuture(PaymentResponseDto.failed("INVALID_CVV",
                        "Invalid security code"));
            }

            // 3. Имитация связи с банком
            simulateNetworkDelay(2000, 4000);

            // 4. Проверка баланса (случайная)
            boolean hasSufficientFunds = Math.random() > 0.3; // 70% успеха

            if (hasSufficientFunds) {
                String transactionId = generateTransactionId();
                return CompletableFuture.completedFuture(PaymentResponseDto.success(transactionId,
                        "Payment approved"));
            } else {
                return CompletableFuture.completedFuture(PaymentResponseDto.failed("INSUFFICIENT_FUNDS",
                        "Insufficient funds in account"));
            }
        } catch (PaymentInterruptedException e) {
            // ОБРАБОТКА ПРЕРЫВАНИЯ
            log.error("Payment processing was interrupted", e);
            return CompletableFuture.completedFuture(PaymentResponseDto.failed("PAYMENT_INTERRUPTED",
                    "Payment processing was interrupted"));
        } catch (Exception e) {
            // ОБРАБОТКА ЛЮБЫХ ДРУГИХ ОШИБОК
            log.error("Unexpected error in payment processing", e);
            return CompletableFuture.completedFuture(PaymentResponseDto.failed("SYSTEM_ERROR",
                    "Payment processing failed"));
        } finally {
            log.info("=== ASYNC METHOD COMPLETED ===");
        }
    }

    private void simulateNetworkDelay(int minMs, int maxMs) {
        try {
            int delay = minMs + (int)(Math.random() * (maxMs - minMs));
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PaymentInterruptedException("Payment interrupted", e);
        }
    }

    private boolean isValidCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.isBlank() || cardNumber.length() != 16) {
            return false;
        }
        return cardNumber.matches("\\d{16}");
    }

    private boolean isValidExpiryDate(String expiryDate) {
        if (expiryDate == null || !expiryDate.matches("\\d{2}/\\d{2}")) {
            return false;
        }
        // Простая проверка - карта не просрочена до 2026
        return expiryDate.compareTo("01/26") >= 0;
    }

    private boolean isValidCvv(String cvv) {
        return cvv != null && cvv.matches("\\d{3}");
    }

    private String generateTransactionId() {
        return "TXN_" +
                System.currentTimeMillis() + "_" +
                (char)('A' + (int)(Math.random() * 26)) +
                (int)(Math.random() * 1000);
    }
}