package by.egrius.pizzaShop.mapper;

import by.egrius.pizzaShop.dto.pizza_ingredient.PizzaIngredientReadDto;
import by.egrius.pizzaShop.entity.PizzaIngredient;
import by.egrius.pizzaShop.mapper.ingredient.IngredientReadMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PizzaIngredientReadMapper implements BaseMapper<PizzaIngredient, PizzaIngredientReadDto>{

    private final IngredientReadMapper ingredientReadMapper;

    @Override
    public PizzaIngredientReadDto map(PizzaIngredient object) {
        return new PizzaIngredientReadDto(
                object.getId(),
                ingredientReadMapper.map(object.getIngredient()),
                object.getWeightGrams()
        );
    }
}
