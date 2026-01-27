package by.egrius.pizzaShop.controller.handler;

import by.egrius.pizzaShop.controller.admin.AdminPizzaController;
import by.egrius.pizzaShop.controller.customer.PublicPizzaController;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice(assignableTypes = {PublicPizzaController.class, AdminPizzaController.class})
public class ExceptionHandlerPizzaControllerAdvice {

}
