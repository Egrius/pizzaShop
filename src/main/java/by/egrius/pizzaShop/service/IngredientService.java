package by.egrius.pizzaShop.service;

import by.egrius.pizzaShop.dto.ingredient.IngredientCreateDto;
import by.egrius.pizzaShop.dto.ingredient.IngredientReadDto;
import by.egrius.pizzaShop.dto.ingredient.IngredientUpdateDto;
import by.egrius.pizzaShop.dto.pizza.PizzaCreateDto;
import by.egrius.pizzaShop.dto.pizza.PizzaReadDto;
import by.egrius.pizzaShop.entity.Ingredient;
import by.egrius.pizzaShop.event.publisher.IngredientEventPublisher;
import by.egrius.pizzaShop.exception.IngredientAlreadyExistsException;
import by.egrius.pizzaShop.mapper.ingredient.IngredientCreateMapper;
import by.egrius.pizzaShop.mapper.ingredient.IngredientReadMapper;
import by.egrius.pizzaShop.mapper.ingredient.IngredientUpdateMapper;
import by.egrius.pizzaShop.repository.IngredientRepository;
import by.egrius.pizzaShop.repository.PizzaIngredientRepository;
import by.egrius.pizzaShop.repository.PizzaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Сервис для управления ингредиентами ({@link Ingredient}).
 * <p>
 * <b>Основные функции:</b>
 * <ul>
 *   <li>CRUD операции над ингредиентами</li>
 *   <li>Пагинация и поиск по ID</li>
 *   <li>Публикация событий при изменении цены ингредиента</li>
 * </ul>
 * <p>
 * <b>Ключевые особенности:</b>
 * <ul>
 *   <li>При изменении цены ингредиента автоматически уведомляет систему
 *       через {@link IngredientEventPublisher} для перерасчёта цен пицц</li>
 *   <li>Запрещает удаление ингредиентов, используемых в существующих пиццах</li>
 *   <li>Гарантирует уникальность названий ингредиентов</li>
 * </ul>
 * <p>
 * <b>Ключевые зависимости:</b>
 * <ul>
 *   <li>{@link IngredientRepository} - доступ к данным</li>
 *   <li>{@link IngredientEventPublisher} - публикация событий об изменении цены</li>
 *   <li>{@link PizzaIngredientRepository} - проверка использования ингредиентов</li>
 * </ul>
 *
 * @see by.egrius.pizzaShop.controller.admin.AdminIngredientController
 * @see by.egrius.pizzaShop.event.listener.IngredientPriceChangedEventListener
 * @see IngredientCreateDto
 * @see IngredientUpdateDto
 * @see IngredientReadDto
 */
@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class IngredientService {
    private final PizzaIngredientRepository pizzaIngredientRepository;
    private final IngredientRepository ingredientRepository;
    private final IngredientReadMapper ingredientReadMapper;
    private final IngredientCreateMapper ingredientCreateMapper;
    private final IngredientUpdateMapper ingredientUpdateMapper;

    private final IngredientEventPublisher ingredientEventPublisher;

    /**
     * Возвращает страницу со всеми ингредиентами.
     * <p>
     * Используется для отображения списка ингредиентов в админ-панели.
     *
     * @param page номер страницы (начинается с 0)
     * @param size количество элементов на странице
     * @return {@link Page} с {@link IngredientReadDto}
     *
     * @see #getIngredientsByIds(Set)
     */
    public Page<IngredientReadDto> getAllIngredients(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ingredientRepository.findAll(pageable).map(ingredientReadMapper::map);
    }

    /**
     * Находит ингредиенты по набору ID.
     * <p>
     * Используется при создании/обновлении пиццы для валидации существования ингредиентов.
     *
     * @param ingredientIds набор ID ингредиентов
     * @return список {@link IngredientReadDto}
     * @throws IllegalArgumentException если передан пустой список ID
     * @throws EntityNotFoundException если не найдены один или несколько ингредиентов
     *
     * @see PizzaService#createPizza(PizzaCreateDto)
     */
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
    @CacheEvict(value = "ingredientsPage", allEntries = true)
    //@CachePut(value = "ingredients", key = "#result.id()")
    public IngredientReadDto createIngredient(IngredientCreateDto ingredientCreateDto) {
        log.info("Передано DTO для создания ингредиента с именем \"{}\"", ingredientCreateDto.name());

        if(ingredientRepository.existsByName(ingredientCreateDto.name())) {
            log.warn("Попытка создания ингредиента с существующим именем \"{}\"",
                    ingredientCreateDto.name());
            throw new IngredientAlreadyExistsException("Ингредиент с именем \""
                    + ingredientCreateDto.name() + "\" уже существует в БД");
        }

        Ingredient ingredient = ingredientCreateMapper.map(ingredientCreateDto);
        ingredientRepository.save(ingredient);

        log.info("Ингридиент успешно сохранён в БД");

        return ingredientReadMapper.map(ingredient);
    }

    /**
     * Обновляет ингредиент, включая публикацию события при изменении цены.
     * <p>
     * <b>Особенности:</b>
     * <ul>
     *   <li>Проверяет уникальность нового имени</li>
     *   <li>При изменении цены публикует {@link by.egrius.pizzaShop.event.IngredientPriceChangedEvent}</li>
     *   <li>Использует optimistic locking через поле {@code version}</li>
     * </ul>
     * <p>
     * TODO: Обрабатывать событие только после коммита транзакции
     * TODO: Добавить проверку версий для предотвращения гонок
     *
     * @param id ID обновляемого ингредиента
     * @param ingredientUpdateDto DTO с новыми данными
     * @return {@link IngredientReadDto} обновлённого ингредиента
     * @throws EntityNotFoundException если ингредиент не найден
     * @throws IngredientAlreadyExistsException если новое имя уже используется
     *
     * @see IngredientEventPublisher#publishIngredientPriceChangedEvent(Long, Long, BigDecimal, BigDecimal)
     */
    @Transactional
    public IngredientReadDto updateIngredient(Long id, IngredientUpdateDto ingredientUpdateDto) {
        Ingredient ingredientToUpdate = ingredientRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Ингредиент с id - " + id + " не найден в БД"));

        if(ingredientRepository.existsByName(ingredientUpdateDto.name())) {
            log.warn("Попытка создания ингредиента с существующим именем \"{}\"",
                    ingredientUpdateDto.name());

            throw new IngredientAlreadyExistsException("Ингредиент с именем \""
                    + ingredientUpdateDto.name() + "\" уже существует в БД");
        }

        BigDecimal priceBefore = ingredientToUpdate.getPrice();
        Long version = ingredientToUpdate.getVersion();

        ingredientUpdateMapper.map(ingredientUpdateDto, ingredientToUpdate);

        log.info("Ингредиент с id {} успешно обновлен", id);

        BigDecimal priceAfter = ingredientToUpdate.getPrice();

        IngredientReadDto ingredientReadDto = ingredientReadMapper.map(ingredientToUpdate);

        if (priceBefore.compareTo(priceAfter) != 0) {
            log.info("Цена изменилась ({} -> {}), публикуем событие об изменении цены",
                    priceBefore, priceAfter);

            ingredientEventPublisher.publishIngredientPriceChangedEvent(ingredientReadDto.id(), ingredientToUpdate.getVersion() + 1, priceAfter, priceBefore);
        }

        return ingredientReadDto;
    }

    /**
     * Удаляет ингредиент, если он не используется в пиццах.
     * <p>
     * <b>Бизнес-правило:</b> Нельзя удалить ингредиент, который используется
     * хотя бы в одной пицце (даже если пицца неактивна).
     *
     * @param id ID удаляемого ингредиента
     * @throws EntityNotFoundException если ингредиент не найден
     * @throws IllegalStateException если ингредиент используется в пиццах
     *
     * @see PizzaIngredientRepository#countPizzaUses(Long)
     */
    @Transactional
    public void deleteIngredient(Long id) {
        log.info("Передано id на удаление ингредиента - {}", id);

        Ingredient ingredientToDelete = ingredientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Ингредиент для удаления с id %d не найден в БД", id)));

        long pizzaUses =  pizzaIngredientRepository.countPizzaUses(id);

        if(pizzaUses > 0) {
            log.warn("Попытка удалить ингредиент {}, используемый в {} пиццах",
                    ingredientToDelete.getName(), pizzaUses);
            throw new IllegalStateException(
                    String.format("Невозможно удалить ингредиент '%s', так как он используется в %d пиццах",
                            ingredientToDelete.getName(), pizzaUses));
        }

        String ingredientName = ingredientToDelete.getName();

        log.info("Найден ингредиент для удаления, id - {}, название - {}", id, ingredientName);

        ingredientRepository.delete(ingredientToDelete);

        log.info("Ингредиент с id - {}, название - {}, был успешно удален", id, ingredientName);
    }
}