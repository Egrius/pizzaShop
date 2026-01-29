package by.egrius.pizzaShop.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AtLeastOneFieldNotNullAndValidPizzaUpdateValidator.class)
public @interface AtLeastOneFieldNotNullAndValidPizzaUpdate {
    String message() default "Хотя бы одно поле должно быть заполнено";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
