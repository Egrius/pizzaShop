package by.egrius.pizzaShop.controller.handler;

import by.egrius.pizzaShop.controller.customer.CartController;
import by.egrius.pizzaShop.dto.error.ExceptionDto;
import by.egrius.pizzaShop.exception.CartItemNotFoundException;
import by.egrius.pizzaShop.exception.PizzaAlreadyExistsException;
import by.egrius.pizzaShop.exception.PizzaNotFoundException;
import by.egrius.pizzaShop.exception.PizzaSizeNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDateTime;

@ControllerAdvice(assignableTypes = {CartController.class})
public class ExceptionHandlerCartControllerAdvice {

    @ExceptionHandler(PizzaNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ExceptionDto onPizzaAlreadyExists(HttpServletRequest request, Exception e) {
        return new ExceptionDto(
                e.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI(),
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(PizzaSizeNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ExceptionDto onPizzaSizeNotFound(HttpServletRequest request, Exception e) {
        return new ExceptionDto(
                e.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI(),
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(CartItemNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ExceptionDto onCartItemNotFound(HttpServletRequest request, Exception e) {
        return new ExceptionDto(
                e.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                request.getRequestURI(),
                LocalDateTime.now()
        );
    }
}
