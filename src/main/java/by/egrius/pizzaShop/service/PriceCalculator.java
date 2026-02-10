package by.egrius.pizzaShop.service;

import by.egrius.pizzaShop.entity.Ingredient;
import by.egrius.pizzaShop.entity.PizzaSizeEnum;
import by.egrius.pizzaShop.entity.SizeTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * Калькулятор для расчёта цены создаваемой пиццы.
 * Определяет константы для цен составляющих пиццу компонентов.
 */
@Component
public class PriceCalculator {

    // БОЛЕЕ РЕАЛИСТИЧНЫЕ ЦЕНЫ
    private static final BigDecimal DOUGH_PRICE_PER_KG = new BigDecimal("5.00");  // Тесто подешевле
    private static final BigDecimal SAUCE_PRICE_PER_KG = new BigDecimal("3.00");  // Соус подешевле

    // РЕАЛИСТИЧНАЯ НАЦЕНКА (50-100%)
    private static final BigDecimal MARKUP_PERCENTAGE = new BigDecimal("0.8");    // 80% наценка

    // Процентное соотношение (исправлено)
    private static final BigDecimal DOUGH_PERCENTAGE = new BigDecimal("0.45");    // 45% теста
    private static final BigDecimal SAUCE_PERCENTAGE = new BigDecimal("0.10");    // 10% соуса

    // Минимальные цены
    private static final BigDecimal MIN_PRICE_SMALL = new BigDecimal("12.90");
    private static final BigDecimal MIN_PRICE_MEDIUM = new BigDecimal("16.90");
    private static final BigDecimal MIN_PRICE_LARGE = new BigDecimal("20.90");

    private static final BigDecimal PRICE_SUFFIX = new BigDecimal("0.90");
    private static final BigDecimal GRAMS_TO_KG = new BigDecimal("1000");

    public BigDecimal calculatePrice(
            SizeTemplate sizeTemplate,
            Map<Long, Ingredient> ingredientsMap,
            Map<Long, Integer> ingredientWeights) {

        // 1. Стоимость ингредиентов для БАЗОВОГО размера (без множителя)
        BigDecimal ingredientsCost = calculateIngredientsCost(ingredientsMap, ingredientWeights);

        // 2. Вес пиццы с учетом множителя размера
        BigDecimal baseWeightKg = BigDecimal.valueOf(sizeTemplate.getWeightGrams())
                .divide(GRAMS_TO_KG, 4, RoundingMode.HALF_EVEN);

        // 3. Расчет для конкретного размера
        BigDecimal doughCost = baseWeightKg
                .multiply(DOUGH_PERCENTAGE)
                .multiply(DOUGH_PRICE_PER_KG)
                .multiply(sizeTemplate.getSizeMultiplier());

        BigDecimal sauceCost = baseWeightKg
                .multiply(SAUCE_PERCENTAGE)
                .multiply(SAUCE_PRICE_PER_KG)
                .multiply(sizeTemplate.getSizeMultiplier());

        BigDecimal scaledIngredientsCost = ingredientsCost
                .multiply(sizeTemplate.getSizeMultiplier());

        // 4. Полная себестоимость для данного размера
        BigDecimal totalCost = scaledIngredientsCost
                .add(doughCost)
                .add(sauceCost);

        // 5. Добавляем наценку
        BigDecimal priceWithMarkup = totalCost
                .multiply(BigDecimal.ONE.add(MARKUP_PERCENTAGE));

        // 6. Округление и проверка минимальной цены
        return roundToNicePrice(priceWithMarkup, sizeTemplate.getSizeName());
    }

    private BigDecimal calculateIngredientsCost(
            Map<Long, Ingredient> ingredientsMap,
            Map<Long, Integer> ingredientWeights) {

        BigDecimal totalCost = BigDecimal.ZERO;

        for (Map.Entry<Long, Integer> entry : ingredientWeights.entrySet()) {
            Ingredient ingredient = ingredientsMap.get(entry.getKey());

            if (ingredient == null) {
                continue;
            }

            BigDecimal weightKg = BigDecimal.valueOf(entry.getValue())
                    .divide(GRAMS_TO_KG, 4, RoundingMode.HALF_EVEN);

            BigDecimal ingredientCost = ingredient.getPrice().multiply(weightKg);
            totalCost = totalCost.add(ingredientCost);
        }

        return totalCost;
    }

    private BigDecimal roundToNicePrice(BigDecimal price, PizzaSizeEnum size) {
        // Округляем до целого (12.34 -> 12.00)
        BigDecimal roundedToInteger = price.setScale(0, RoundingMode.HALF_UP);

        // Добавляем .90
        BigDecimal result = roundedToInteger.add(PRICE_SUFFIX);

        // Проверяем минимальную цену
        BigDecimal minPrice = switch (size) {
            case SMALL -> MIN_PRICE_SMALL;
            case MEDIUM -> MIN_PRICE_MEDIUM;
            case LARGE -> MIN_PRICE_LARGE;
        };

        return result.max(minPrice);
    }
}