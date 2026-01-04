package by.egrius.pizzaShop.mapper.pizza;

import by.egrius.pizzaShop.dto.pizza.PizzaUpdateDto;
import by.egrius.pizzaShop.entity.Pizza;
import by.egrius.pizzaShop.mapper.BaseMapper;
import org.springframework.stereotype.Component;

@Component
public class PizzaUpdateMapper implements BaseMapper<PizzaUpdateDto, Pizza> {
    @Override
    public Pizza map(PizzaUpdateDto object) {
        throw new UnsupportedOperationException("Данный метод не поддерживается, используйте map(PizzaUpdateDto fromObject, Pizza toObject) ");
    }

    @Override
    public Pizza map(PizzaUpdateDto fromObject, Pizza toObject) {
        copy(fromObject, toObject);
        return toObject;
    }

    private void copy(PizzaUpdateDto fromObject, Pizza toObject) {
        if(fromObject.name() != null && !fromObject.name().isBlank()) {
            toObject.setName(fromObject.name());
        }
        if(fromObject.description() != null && !fromObject.description().isBlank()) {
            toObject.setDescription(fromObject.description());
        }
        if(fromObject.imageUrl() != null && !fromObject.imageUrl().isBlank()) {
            toObject.setImageUrl(fromObject.imageUrl());
        }
        if(fromObject.category() != null && !fromObject.category().isBlank()) {
            toObject.setCategory(fromObject.category());
        }
        if(fromObject.available() != null) {
            toObject.setAvailable(fromObject.available());
        }
        if(fromObject.cookingTimeMinutes() != null) {
            toObject.setCookingTimeMinutes(fromObject.cookingTimeMinutes());
        }
    }
}
