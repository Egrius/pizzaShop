package by.egrius.pizzaShop.event.listener;

import by.egrius.pizzaShop.entity.Ingredient;
import by.egrius.pizzaShop.event.IngredientPriceChangedEvent;
import by.egrius.pizzaShop.repository.IngredientRepository;
import by.egrius.pizzaShop.service.PizzaPriceRecalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class IngredientPriceChangedEventListener {

    private final PizzaPriceRecalculationService pizzaPriceRecalculationService;
    private final IngredientRepository ingredientRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onApplicationEvent(IngredientPriceChangedEvent event) {
        log.info("Получено событие для ингредиента '"
                + event.getIngredientId());

        Ingredient ingredient = ingredientRepository.findById(event.getIngredientId())
                .orElseThrow();

        if (!ingredient.getVersion().equals(event.getVersion())) {
            log.warn("Ingredient {} version mismatch, skipping recalculation",
                    event.getIngredientId());
            return;
        }

        log.info("Запускаем асинхронный перерасчёт цены");

        pizzaPriceRecalculationService.recalculatePricesForIngredientAsync(event.getIngredientId());
    }
}