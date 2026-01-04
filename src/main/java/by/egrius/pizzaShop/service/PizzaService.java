package by.egrius.pizzaShop.service;

import by.egrius.pizzaShop.dto.pizza.PizzaCreateDto;
import by.egrius.pizzaShop.dto.pizza.PizzaReadDto;
import by.egrius.pizzaShop.dto.pizza.PizzaUpdateDto;
import by.egrius.pizzaShop.entity.Ingredient;
import by.egrius.pizzaShop.entity.Pizza;
import by.egrius.pizzaShop.exception.PizzaAlreadyExistsException;
import by.egrius.pizzaShop.mapper.pizza.PizzaCreateMapper;
import by.egrius.pizzaShop.mapper.pizza.PizzaReadMapper;
import by.egrius.pizzaShop.mapper.pizza.PizzaUpdateMapper;
import by.egrius.pizzaShop.repository.IngredientRepository;
import by.egrius.pizzaShop.repository.PizzaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
@AllArgsConstructor
public class PizzaService {

    private final PizzaRepository pizzaRepository;
    private final PizzaCreateMapper pizzaCreateMapper;
    private final PizzaReadMapper pizzaReadMapper;
    private final PizzaUpdateMapper pizzaUpdateMapper;
    private final IngredientRepository ingredientRepository;

    //TODO продумать максимальное и минимальное количество ингредиентов для создания пиццы!

    @Transactional
    public PizzaReadDto createPizza(PizzaCreateDto createDto) {

        log.info("Передано DTO на создание пиццы с именем {}", createDto.name());

        if (createDto.ingredientIds().isEmpty()) {
            log.warn("Попытка создать пиццу '{}' без ингредиентов", createDto.name());
            throw new IllegalArgumentException("Пицца должна содержать ингредиенты");
        }

        if (pizzaRepository.existsByName(createDto.name())) {
            log.warn("Пицца с именем \"{}\" уже существует в БД", createDto.name());
            throw new PizzaAlreadyExistsException("Пицца с таким именем уже существует");
        }

        List<Ingredient> ingredientsFound = ingredientRepository.findAllById(createDto.ingredientIds());

        if (ingredientsFound.size() != createDto.ingredientIds().size()) {
            Set<Long> foundIds = ingredientsFound.stream()
                    .map(Ingredient::getId)
                    .collect(Collectors.toSet());

            List<Long> notFoundIngredients = createDto.ingredientIds().stream()
                    .filter(id -> !foundIds.contains(id))
                    .toList();

            if (!notFoundIngredients.isEmpty()) {
                throw new EntityNotFoundException(
                        "Не найдены ингредиенты с id: " + notFoundIngredients.stream()
                                .map(String::valueOf)
                                .collect(Collectors.joining(", "))
                );
            }
        }

        Pizza pizza = pizzaCreateMapper.map(createDto);
        pizza.setIngredients(ingredientsFound);

        pizzaRepository.save(pizza);
        log.info("Пицца \"{}\" сохранена в БД, количество переданных ингредиентов - {}", createDto.name(), ingredientsFound.size());

        return pizzaReadMapper.map(pizza);
    }

    @Transactional
    public void updatePizzaById(Long id, PizzaUpdateDto updateDto) {
        log.info("Передано DTO на обновление пиццы с именем {}", updateDto.name());

        Pizza pizzaToUpdate = pizzaRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(
                String.format("Пицца для обновления с id %d не найдена в БД", id)));

        pizzaUpdateMapper.map(updateDto,pizzaToUpdate);

        pizzaRepository.save(pizzaToUpdate);
        log.info("Пицца с id {} успешно обновлена", id);
    }

    @Transactional
    public void deletePizzaById(Long id) {
        log.info("Передано id на удаление пиццы - {}", id);

        Pizza pizzaToDelete = pizzaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Пицца для удаления с id %d не найдена в БД", id)));

        Long pizzaId = pizzaToDelete.getId();
        String pizzaName = pizzaToDelete.getName();

        log.info("Найдена пицца для удаления, id - {}, название - {}", pizzaId, pizzaName);

        pizzaRepository.delete(pizzaToDelete);

        log.info("Пицца с id - {}, название - {}, была успешно удалена", pizzaId, pizzaName);
    }
}
