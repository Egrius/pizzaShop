package by.egrius.pizzaShop.repository;

import by.egrius.pizzaShop.dto.pizza.PizzaCardDto;
import by.egrius.pizzaShop.filter.PizzaFilter;
import java.util.List;

public interface PizzaFilterRepository {
    List<PizzaCardDto> findByFilter(PizzaFilter filter);
}
