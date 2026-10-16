package by.egrius.pizzaShop.exception.handler;

import by.egrius.pizzaShop.controller.admin.AdminPizzaController;
import by.egrius.pizzaShop.controller.customer.PublicPizzaController;
import by.egrius.pizzaShop.dto.error.ExceptionDto;
import by.egrius.pizzaShop.dto.error.ViolationDto;
import by.egrius.pizzaShop.exception.PizzaAlreadyExistsException;
import by.egrius.pizzaShop.exception.PizzaNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDateTime;

@ControllerAdvice(assignableTypes = {PublicPizzaController.class, AdminPizzaController.class})
public class ExceptionHandlerPizzaControllerAdvice {

    @ExceptionHandler(PizzaAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    @ResponseBody
    public ExceptionDto onPizzaAlreadyExists(HttpServletRequest request, Exception e) {
        return new ExceptionDto(
                e.getMessage(),
                HttpStatus.CONFLICT.value(),
                request.getRequestURI(),
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(PizzaNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ExceptionDto onPizzaNotFound(HttpServletRequest request, Exception e) {
        return new ExceptionDto(
                e.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                request.getRequestURI(),
                LocalDateTime.now()
        );
    }

}
