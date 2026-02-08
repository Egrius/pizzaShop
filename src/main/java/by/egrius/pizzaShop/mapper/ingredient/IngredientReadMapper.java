package by.egrius.pizzaShop.mapper.ingredient;

import by.egrius.pizzaShop.dto.ingredient.IngredientReadDto;
import by.egrius.pizzaShop.entity.Ingredient;
import by.egrius.pizzaShop.mapper.BaseMapper;
import org.springframework.stereotype.Component;

@Component
public class IngredientReadMapper implements BaseMapper<Ingredient, IngredientReadDto> {
    @Override
    public IngredientReadDto map(Ingredient object) {
        return new IngredientReadDto(
                object.getId(),
                object.getName(),
                object.getDescription(),
                object.getPrice(),
                object.isAvailable(),
                object.getCreatedAt()
               // object.getVersion()
        );
    }
}
