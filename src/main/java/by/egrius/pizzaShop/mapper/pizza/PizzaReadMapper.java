package by.egrius.pizzaShop.mapper.pizza;

import by.egrius.pizzaShop.dto.pizza.PizzaReadDto;
import by.egrius.pizzaShop.entity.Pizza;
import by.egrius.pizzaShop.mapper.BaseMapper;
import by.egrius.pizzaShop.mapper.PizzaIngredientReadMapper;
import by.egrius.pizzaShop.mapper.ingredient.IngredientReadMapper;
import by.egrius.pizzaShop.mapper.pizza_size.PizzaSizeReadMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class PizzaReadMapper implements BaseMapper<Pizza, PizzaReadDto> {

    private final PizzaIngredientReadMapper pizzaIngredientReadMapper;
    private final PizzaSizeReadMapper pizzaSizeReadMapper;

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
                //Маппинг PizzaIngredient
                object.getPizzaIngredients().stream().map(pizzaIngredientReadMapper::map).toList(),
                //Маппинг PizzaSize
                object.getPizzaSizes().stream().map(pizzaSizeReadMapper::map).toList()

        );
    }
}
