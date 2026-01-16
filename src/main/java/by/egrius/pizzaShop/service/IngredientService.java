package by.egrius.pizzaShop.service;

import by.egrius.pizzaShop.dto.ingredient.IngredientCreateDto;
import by.egrius.pizzaShop.dto.ingredient.IngredientReadDto;
import by.egrius.pizzaShop.dto.ingredient.IngredientUpdateDto;
import by.egrius.pizzaShop.entity.Ingredient;
import by.egrius.pizzaShop.exception.IngredientAlreadyExistsException;
import by.egrius.pizzaShop.mapper.ingredient.IngredientCreateMapper;
import by.egrius.pizzaShop.mapper.ingredient.IngredientReadMapper;
import by.egrius.pizzaShop.mapper.ingredient.IngredientUpdateMapper;
import by.egrius.pizzaShop.repository.IngredientRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class IngredientService {
    private final IngredientRepository ingredientRepository;
    private final IngredientReadMapper ingredientReadMapper;
    private final IngredientCreateMapper ingredientCreateMapper;
    private final IngredientUpdateMapper ingredientUpdateMapper;

    // TODO добавление
    // TODO обновление
    // TODO удаление

    public Page<IngredientReadDto> getAllIngredients(int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber,pageSize);
        return ingredientRepository.findAll(pageable).map(ingredientReadMapper::map);
    }

    public List<IngredientReadDto> getIngredientsByIds(Set<Long> ingredientIds) {
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

    @Transactional
    public IngredientReadDto createIngredient(IngredientCreateDto ingredientCreateDto) {
        log.info("Передано DTO для создания ингредиента с именем \"{}\"", ingredientCreateDto.name());

        if(ingredientRepository.existsByName(ingredientCreateDto.name())) {
            log.warn("Попытка создать создания ингредиента с существующим именем \"{}\"",
                    ingredientCreateDto.name());
            throw new IngredientAlreadyExistsException("Ингредиент с именем \""
                    + ingredientCreateDto.name() + "\" уже существует в БД");
        }

        Ingredient ingredient = ingredientCreateMapper.map(ingredientCreateDto);
        ingredientRepository.save(ingredient);

        log.info("Ингридиент успешно сохранён в БД");

        return ingredientReadMapper.map(ingredient);
    }

    @Transactional
    public IngredientReadDto updateIngredient(Long id, IngredientUpdateDto ingredientUpdateDto) {
        Ingredient ingredientToUpdate = ingredientRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Ингредиент с id - " + id + " не найден в БД"));

        ingredientUpdateMapper.map(ingredientUpdateDto, ingredientToUpdate);

        Ingredient updatedIngredient = ingredientRepository.save(ingredientToUpdate);

        log.info("Ингредиент с id {} успешно обновлен", id);

        return ingredientReadMapper.map(updatedIngredient);
    }

    @Transactional
    public void deleteIngredient(Long id) {
        log.info("Передано id на удаление ингредиента - {}", id);

        Ingredient ingredientToDelete = ingredientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Ингредиент для удаления с id %d не найден в БД", id)));

        long pizzaUses = ingredientRepository.countPizzaUses(id);

        if(pizzaUses > 0) {
            log.warn("Попытка удалить ингредиент {}, используемый в {} пиццах",
                    ingredientToDelete.getName(), pizzaUses);
            throw new IllegalStateException(
                    String.format("Невозможно удалить ингредиент '%s', так как он используется в %d пиццах",
                            ingredientToDelete.getName(), pizzaUses));
        }

        Long ingredientId = ingredientToDelete.getId();
        String ingredientName = ingredientToDelete.getName();

        log.info("Найден ингредиент для удаления, id - {}, название - {}", ingredientId, ingredientName);

        ingredientRepository.delete(ingredientToDelete);

        log.info("Ингредиент с id - {}, название - {}, был успешно удален", ingredientId, ingredientName);
    }
}
