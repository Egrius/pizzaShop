package by.egrius.pizzaShop.annotation;

import by.egrius.pizzaShop.dto.pizza.PizzaUpdateDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.stream.Stream;

public class AtLeastOneFieldNotNullAndValidPizzaUpdateValidator
        implements ConstraintValidator<AtLeastOneFieldNotNullAndValidPizzaUpdate,PizzaUpdateDto> {
    @Override
    public boolean isValid(PizzaUpdateDto pizzaUpdateDto, ConstraintValidatorContext constraintValidatorContext) {
        if (pizzaUpdateDto == null) return false;

        boolean valid = false;

        boolean anyMatchStringFields = Stream.of(pizzaUpdateDto.name(),
                pizzaUpdateDto.description(),
                pizzaUpdateDto.imageUrl(),
                pizzaUpdateDto.category())
                .anyMatch(field -> field != null && !field.isBlank());

        boolean anyMatchBooleanField = pizzaUpdateDto.available() != null;
        boolean anyMatchIntegerField = pizzaUpdateDto.cookingTimeMinutes() != null;

        return anyMatchStringFields || anyMatchBooleanField || anyMatchIntegerField;
    }
}
