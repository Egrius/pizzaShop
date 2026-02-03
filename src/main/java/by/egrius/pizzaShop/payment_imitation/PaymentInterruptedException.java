package by.egrius.pizzaShop.payment_imitation;

public class PaymentInterruptedException extends RuntimeException {
    public PaymentInterruptedException(String message) {
        super(message);
    }

    public PaymentInterruptedException(String message, Throwable cause) {
        super(message, cause);
    }
}
