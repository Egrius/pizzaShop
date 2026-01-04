package by.egrius.pizzaShop.exception;

public class PizzaAlreadyExistsException extends RuntimeException{
    public PizzaAlreadyExistsException(String message) {
        super(message);
    }
}
