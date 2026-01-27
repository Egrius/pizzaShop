package by.egrius.pizzaShop.controller.handler;

import by.egrius.pizzaShop.dto.error.ValidationErrorDto;
import by.egrius.pizzaShop.dto.error.ViolationDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandlerControllerAdvice {
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    ValidationErrorDto onConstraintValidationException(ConstraintViolationException e) {
        System.out.println("Вызван обработчик onConstraintValidationException\n");
        ValidationErrorDto error = new ValidationErrorDto();
        for(ConstraintViolation v : e.getConstraintViolations()) {
            error.getViolations().add(
                    new ViolationDto(v.getPropertyPath().toString(), v.getMessage())
            );
        }
        return error;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ValidationErrorDto handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        System.out.println("Вызван обработчик handleMethodArgumentNotValid\n");
        ValidationErrorDto error = new ValidationErrorDto();

        e.getBindingResult().getFieldErrors().forEach(
                fieldError -> error.getViolations().add(
                        new ViolationDto(fieldError.getField(), fieldError.getDefaultMessage())
                )
        );
        return error;
    }
}
