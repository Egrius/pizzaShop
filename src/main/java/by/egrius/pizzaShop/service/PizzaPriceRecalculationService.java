package by.egrius.pizzaShop.service;

import by.egrius.pizzaShop.entity.Ingredient;
import by.egrius.pizzaShop.entity.Pizza;
import by.egrius.pizzaShop.entity.PizzaIngredient;
import by.egrius.pizzaShop.repository.PizzaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PizzaPriceRecalculationService {

    private final PizzaRepository pizzaRepository;
    private final PriceCalculator priceCalculator;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recalculatePricesForIngredient(Long ingredientId) {
        log.info("Перерасчет цен для пицц, содержащих ингредиент ID: {}", ingredientId);

        var pizzas = pizzaRepository.findPizzasByIngredientId(ingredientId);

        if (pizzas.isEmpty()) {
            log.info("Пиццы с ингредиентом ID: {} не найдены", ingredientId);
            return;
        }

        log.info("Найдено {} пицц для перерасчета", pizzas.size());

        pizzas.forEach(this::recalculatePizzaPrice);

        log.info("Перерасчет цен завершен для {} пицц", pizzas.size());
    }

    @Async("priceRecalculationExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recalculatePricesForIngredientAsync(Long ingredientId) {
        log.info("=== START ASYNC ===");
        log.debug("Начало асинхронного перерасчёта цены для ингредиента с ID - {}", ingredientId);

        //entityManager.clear();

        recalculatePricesForIngredient(ingredientId);

        log.debug("Конец асинхронного перерасчёта цены для ингредиента с ID - {}", ingredientId);
        log.info("=== END ASYNC ===");
    }

    private void recalculatePizzaPrice(Pizza pizza) {
        // Подготовка данных для калькулятора
        var ingredientMap = pizza.getPizzaIngredients().stream()
                .map(PizzaIngredient::getIngredient)
                .collect(Collectors.toMap(
                        Ingredient::getId,
                        Function.identity()
                ));

        var weightMap = pizza.getPizzaIngredients().stream()
                .collect(Collectors.toMap(
                        pi -> pi.getIngredient().getId(),
                        PizzaIngredient::getWeightGrams
                ));

        // Пересчет цен для каждого размера пиццы
        pizza.getPizzaSizes().forEach(pizzaSize -> {
            var newPrice = priceCalculator.calculatePrice(
                    pizzaSize.getSizeTemplate(),
                    ingredientMap,
                    weightMap
            );
            pizzaSize.setPrice(newPrice);
        });

        log.debug("Пересчитаны цены для пиццы ID: {}", pizza.getId());
    }
}
