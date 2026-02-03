package by.egrius.pizzaShop.mapper.ingredient;

import by.egrius.pizzaShop.dto.ingredient.IngredientUpdateDto;
import by.egrius.pizzaShop.entity.Ingredient;
import by.egrius.pizzaShop.mapper.BaseMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class IngredientUpdateMapper implements BaseMapper<IngredientUpdateDto, Ingredient> {

    @Override
    public Ingredient map(IngredientUpdateDto object) {
        throw new UnsupportedOperationException("Данный метод не поддерживается, используйте map(IngredientUpdateDto fromObject, Ingredient toObject)");
    }

    @Override
    public Ingredient map(IngredientUpdateDto fromObject, Ingredient toObject) {
        copy(fromObject, toObject);
        return toObject;
    }

    private void copy(IngredientUpdateDto fromObject, Ingredient toObject) {
        if(fromObject.name() != null && !fromObject.name().isBlank()) toObject.setName(fromObject.name());
        if(fromObject.description() != null && !fromObject.description().isBlank()) toObject.setDescription(fromObject.description());
        if(fromObject.price() != null && fromObject.price().compareTo(BigDecimal.ZERO) > 0) toObject.setPrice(fromObject.price());
        if(fromObject.available() != null) toObject.setAvailable(fromObject.available());
    }
}
