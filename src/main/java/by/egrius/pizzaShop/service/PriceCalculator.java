package by.egrius.pizzaShop.service;

import by.egrius.pizzaShop.entity.Ingredient;
import by.egrius.pizzaShop.entity.PizzaSizeEnum;
import by.egrius.pizzaShop.entity.SizeTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Component
public class PriceCalculator {

    private static final BigDecimal DOUGH_PRICE_PER_KG = new BigDecimal("8.50");  // Тесто
    private static final BigDecimal SAUCE_PRICE_PER_KG = new BigDecimal("4.00");  // Соус
    private static final BigDecimal MARKUP_PERCENTAGE = new BigDecimal("1.5");    // 150% наценка (стандартно)

    // Процентное соотношение
    private static final BigDecimal DOUGH_PERCENTAGE = new BigDecimal("0.5");     // 50% теста
    private static final BigDecimal SAUCE_PERCENTAGE = new BigDecimal("0.1");     // 10% соуса

    // Минимальные цены в BYN (как в Додо)
    private static final BigDecimal MIN_PRICE_SMALL = new BigDecimal("12.90");
    private static final BigDecimal MIN_PRICE_MEDIUM = new BigDecimal("18.90");
    private static final BigDecimal MIN_PRICE_LARGE = new BigDecimal("24.90");

    private static final BigDecimal PRICE_SUFFIX = new BigDecimal("0.90");        // Все цены заканчиваются на .90
    private static final BigDecimal GRAMS_TO_KG = new BigDecimal("1000");

    public BigDecimal calculatePrice(
            SizeTemplate sizeTemplate,
            Map<Long, Ingredient> ingredientsMap,
            Map<Long, Integer> ingredientWeights) {

        BigDecimal ingredientsCost = calculateIngredientsCost(ingredientsMap, ingredientWeights);

        BigDecimal pizzaWeightKg = BigDecimal.valueOf(sizeTemplate.getWeightGrams())
                .divide(GRAMS_TO_KG, 4, RoundingMode.HALF_EVEN);

        BigDecimal doughCost = pizzaWeightKg
                .multiply(DOUGH_PERCENTAGE)
                .multiply(DOUGH_PRICE_PER_KG);

        BigDecimal sauceCost = pizzaWeightKg
                .multiply(SAUCE_PERCENTAGE)
                .multiply(SAUCE_PRICE_PER_KG);

        BigDecimal totalCost = ingredientsCost
                .add(doughCost)
                .add(sauceCost);

        BigDecimal priceWithSizeAndMarkup = totalCost
                .multiply(sizeTemplate.getSizeMultiplier())
                .multiply(BigDecimal.ONE.add(MARKUP_PERCENTAGE));

        return roundToNicePrice(priceWithSizeAndMarkup, sizeTemplate.getSizeName());
    }

    private BigDecimal calculateIngredientsCost(
            Map<Long, Ingredient> ingredientsMap,
            Map<Long, Integer> ingredientWeights) {

        BigDecimal totalCost = BigDecimal.ZERO;

        System.out.println("КАРТА ИНГРЕДИЕНТОВ: " + ingredientsMap);

        System.out.println("КАРТА ИНГРЕДИЕНТОВ И ИХ ВЕСОВ: " + ingredientWeights);

        for (Map.Entry<Long, Integer> entry : ingredientWeights.entrySet()) {
            Ingredient ingredient = ingredientsMap.get(entry.getKey());

            // Переводим граммы в кг
            BigDecimal weightKg = BigDecimal.valueOf(entry.getValue())
                    .divide(GRAMS_TO_KG, 4, RoundingMode.HALF_EVEN);

            BigDecimal ingredientCost = ingredient.getPrice().multiply(weightKg);
            totalCost = totalCost.add(ingredientCost);
        }

        return totalCost;
    }

    private BigDecimal roundToNicePrice(BigDecimal price, PizzaSizeEnum size) {

        System.out.println("--- Результат из калькулятора ДО округления: " + price + " ---");

        BigDecimal roundedToInteger = price.setScale(0, RoundingMode.HALF_UP);

        BigDecimal result = roundedToInteger.add(PRICE_SUFFIX);

        System.out.println("--- Результат из калькулятора: " + result + " ---");

        BigDecimal minPrice = switch (size) {
            case PizzaSizeEnum.SMALL -> MIN_PRICE_SMALL;
            case  PizzaSizeEnum.MEDIUM -> MIN_PRICE_MEDIUM;
            case  PizzaSizeEnum.LARGE -> MIN_PRICE_LARGE;
            default -> BigDecimal.ZERO;
        };

        return result.max(minPrice);
    }
}