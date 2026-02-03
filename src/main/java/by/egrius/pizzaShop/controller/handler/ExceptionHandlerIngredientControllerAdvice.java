package by.egrius.pizzaShop.controller.handler;

import by.egrius.pizzaShop.controller.admin.AdminIngredientController;
import by.egrius.pizzaShop.dto.error.ExceptionDto;
import by.egrius.pizzaShop.exception.IngredientAlreadyExistsException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDateTime;

@ControllerAdvice(assignableTypes = {AdminIngredientController.class})
public class ExceptionHandlerIngredientControllerAdvice {

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    ExceptionDto onIllegalArgumentException(HttpServletRequest request, Exception e) {
        return new ExceptionDto(
                e.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI(),
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(IngredientAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    @ResponseBody
    ExceptionDto onIngredientAlreadyExistsException(HttpServletRequest request, Exception e) {
        return new ExceptionDto(
                e.getMessage(),
                HttpStatus.CONFLICT.value(),
                request.getRequestURI(),
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    ExceptionDto onIllegalStateException(HttpServletRequest request, Exception e) {
        return new ExceptionDto(
                e.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI(),
                LocalDateTime.now()
        );
    }
}
