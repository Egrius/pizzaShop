package by.egrius.pizzaShop.mapper.ingredient;

import by.egrius.pizzaShop.dto.ingredient.IngredientCreateDto;
import by.egrius.pizzaShop.entity.Ingredient;
import by.egrius.pizzaShop.mapper.BaseMapper;
import org.springframework.stereotype.Component;

@Component
public class IngredientCreateMapper implements BaseMapper<IngredientCreateDto, Ingredient> {
    @Override
    public Ingredient map(IngredientCreateDto object) {
        return Ingredient.create(
                object.name(),
                object.description(),
                object.price(),
                object.available()
        );
    }
}