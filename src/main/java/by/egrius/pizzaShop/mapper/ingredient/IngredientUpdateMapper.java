package by.egrius.pizzaShop.mapper.ingredient;

import by.egrius.pizzaShop.dto.ingredient.IngredientUpdateDto;
import by.egrius.pizzaShop.entity.Ingredient;
import by.egrius.pizzaShop.mapper.BaseMapper;

import java.math.BigDecimal;

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
        if(fromObject.newName() != null && !fromObject.newName().isBlank()) toObject.setName(fromObject.newName());
        if(fromObject.newDescription() != null && !fromObject.newDescription().isBlank()) toObject.setDescription(fromObject.newDescription());
        if(fromObject.newPrice() != null && fromObject.newPrice().compareTo(BigDecimal.ZERO) > 0) toObject.setPrice(fromObject.newPrice());
        if(fromObject.available() != null) toObject.setAvailable(fromObject.available());
    }
}
