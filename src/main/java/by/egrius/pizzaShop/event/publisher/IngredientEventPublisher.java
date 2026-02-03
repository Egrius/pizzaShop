package by.egrius.pizzaShop.event.publisher;

import by.egrius.pizzaShop.event.IngredientPriceChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;


@Component
@RequiredArgsConstructor
public class IngredientEventPublisher {
    private final ApplicationEventPublisher eventPublisher;

    public void publishIngredientPriceChangedEvent(Long ingredientId,
                                                   Long version,
                                                   BigDecimal newPrice,
                                                   BigDecimal oldPrice) {
        IngredientPriceChangedEvent event = new IngredientPriceChangedEvent(this, ingredientId, version, newPrice, oldPrice);
        eventPublisher.publishEvent(event);
    }
}
