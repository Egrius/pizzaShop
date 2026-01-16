package by.egrius.pizzaShop.exception;

public class IngredientAlreadyExistsException extends RuntimeException {
    public IngredientAlreadyExistsException(String message) {
        super(message);
    }
}
