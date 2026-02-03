package by.egrius.pizzaShop.event;

import by.egrius.pizzaShop.dto.ingredient.IngredientReadDto;
import lombok.Getter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;

@Getter
@ToString
public class IngredientPriceChangedEvent extends ApplicationEvent {

    private final Long ingredientId;
    private final Long version;
    private final BigDecimal newPrice;
    private final BigDecimal oldPrice;

    public IngredientPriceChangedEvent(Object source,
                                       Long ingredientId,
                                       Long version,
                                       BigDecimal newPrice,
                                       BigDecimal oldPrice) {
        super(source);
        this.ingredientId = ingredientId;
        this.version = version;
        this.newPrice = newPrice;
        this.oldPrice = oldPrice;
    }
}
