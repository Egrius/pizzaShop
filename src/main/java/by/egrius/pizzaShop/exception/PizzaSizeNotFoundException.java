package by.egrius.pizzaShop.exception;

public class PizzaSizeNotFoundException extends RuntimeException {
    public PizzaSizeNotFoundException(String message) {
        super(message);
    }

    public PizzaSizeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}