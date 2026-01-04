package by.egrius.pizzaShop.mapper.pizza;

import by.egrius.pizzaShop.dto.pizza.PizzaReadDto;
import by.egrius.pizzaShop.entity.Pizza;
import by.egrius.pizzaShop.mapper.BaseMapper;
import by.egrius.pizzaShop.mapper.ingredient.IngredientReadMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class PizzaReadMapper implements BaseMapper<Pizza, PizzaReadDto> {

    private final IngredientReadMapper ingredientReadMapper;

    @Override
    public PizzaReadDto map(Pizza object) {
        return new PizzaReadDto(
                object.getId(),
                object.getName(),
                object.getDescription(),
                object.getImageUrl(),
                object.getCategory(),
                object.isAvailable(),
                object.getCookingTimeMinutes(),
                object.getCreatedAt(),
                object.getIngredients().stream().map(ingredientReadMapper::map).toList()
        );
    }
}
