package by.egrius.pizzaShop.mapper.pizza;

import by.egrius.pizzaShop.dto.pizza.PizzaCreateDto;
import by.egrius.pizzaShop.entity.Ingredient;
import by.egrius.pizzaShop.entity.Pizza;
import by.egrius.pizzaShop.mapper.BaseMapper;
import by.egrius.pizzaShop.mapper.ingredient.IngredientCreateMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PizzaCreateMapper implements BaseMapper<PizzaCreateDto, Pizza> {

    @Override
    public Pizza map(PizzaCreateDto object) {
        return Pizza.create(
                object.name(),
                object.description(),
                object.category(),
                object.imageUrl(),
                object.available(),
                object.cookingTimeMinutes()
        );
    }
}
