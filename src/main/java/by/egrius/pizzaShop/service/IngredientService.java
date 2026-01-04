package by.egrius.pizzaShop.service;

import by.egrius.pizzaShop.dto.ingredient.IngredientReadDto;
import by.egrius.pizzaShop.entity.Ingredient;
import by.egrius.pizzaShop.mapper.ingredient.IngredientReadMapper;
import by.egrius.pizzaShop.repository.IngredientRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class IngredientService {
    private final IngredientRepository ingredientRepository;
    private final IngredientReadMapper ingredientReadMapper;

    public List<IngredientReadDto> getIngredients(Set<Long> ingredientIds) {
        log.debug("Запрос ингредиентов по {} IDs", ingredientIds.size());

        if(ingredientIds.isEmpty()) {
            log.error("Пустой список ID ингредиентов");
            throw new IllegalArgumentException("Список ID ингредиентов не может быть пустым");
        }

        List<Ingredient> ingredients = ingredientRepository.findAllById(ingredientIds);
        log.debug("Найдено {} ингредиентов в БД", ingredients.size());

        Set<Long> foundIds = ingredients.stream()
                .map(Ingredient::getId)
                .collect(Collectors.toSet());

        List<Long> notFound = ingredientIds.stream()
                .filter(id -> !foundIds.contains(id))
                .toList();

        if (!notFound.isEmpty()) {
            log.warn("Отсутствуют ингредиенты с ID: {}", notFound);
            throw new EntityNotFoundException(
                    String.format("Не найдены ингредиенты с ID: %s (запрошено: %d, найдено: %d)",
                            notFound, ingredientIds.size(), ingredients.size())
            );
        }

        return ingredients.stream()
                .map(ingredientReadMapper::map)
                .toList();
    }
}
