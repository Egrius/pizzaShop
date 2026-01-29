package by.egrius.pizzaShop.controller.handler;

import by.egrius.pizzaShop.dto.error.ExceptionDto;
import by.egrius.pizzaShop.dto.error.ValidationErrorDto;
import by.egrius.pizzaShop.dto.error.ViolationDto;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.hibernate.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;

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

    @ExceptionHandler({
            TypeMismatchException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ValidationErrorDto handleTypeMismatch(Exception e) {
        System.out.println("Вызван обработчик handleTypeMismatch: " + e.getClass().getSimpleName() + "\n");

        ValidationErrorDto error = new ValidationErrorDto();
        String message = "Неверный формат параметра";

        if (e instanceof MethodArgumentTypeMismatchException mismatchEx) {
            error.getViolations().add(
                    new ViolationDto(mismatchEx.getName(),
                            String.format("Параметр '%s' должен быть числом", mismatchEx.getName()))
            );
        } else {
            error.getViolations().add(
                    new ViolationDto("parameter", message)
            );
        }

        return error;
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ExceptionDto onEntityNotFoundException(HttpServletRequest request, Exception e) {
        return new ExceptionDto(
                e.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                request.getRequestURI(),
                LocalDateTime.now()
        );
    }
}
