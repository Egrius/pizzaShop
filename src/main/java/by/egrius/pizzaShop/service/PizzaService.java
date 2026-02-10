package by.egrius.pizzaShop.service;

import by.egrius.pizzaShop.dto.ingredient.IngredientInfoDto;
import by.egrius.pizzaShop.dto.pizza.*;
import by.egrius.pizzaShop.dto.pizza_size.PizzaSizeInfoDto;
import by.egrius.pizzaShop.dto.size_template.SizeTemplateInfoDto;
import by.egrius.pizzaShop.entity.*;
import by.egrius.pizzaShop.exception.PizzaAlreadyExistsException;
import by.egrius.pizzaShop.exception.PizzaNotFoundException;
import by.egrius.pizzaShop.exception.PriceCalculationException;
import by.egrius.pizzaShop.filter.PizzaFilter;
import by.egrius.pizzaShop.mapper.pizza.PizzaCreateMapper;
import by.egrius.pizzaShop.mapper.pizza.PizzaReadMapper;
import by.egrius.pizzaShop.mapper.pizza.PizzaUpdateMapper;
import by.egrius.pizzaShop.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Сервис для управления сущностями пиццы ({@link Pizza}).
 * <p>
 * <b>Основные функции:</b>
 * <ul>
 *   <li>CRUD операции над пиццами</li>
 *   <li>Фильтрация и пагинация пицц</li>
 *   <li>Расчёт цен с учётом ингредиентов и размеров</li>
 *   <li>Кэширование детальной информации о пиццах</li>
 * </ul>
 * <p>
 * <b>Ключевые зависимости:</b>
 * <ul>
 *   <li>{@link PizzaRepository} - доступ к данным</li>
 *   <li>{@link PriceCalculator} - расчёт цен</li>
 *   <li>{@link IngredientRepository} - управление ингредиентами</li>
 * </ul>
 *
 * @see by.egrius.pizzaShop.controller.customer.PublicPizzaController
 * @see by.egrius.pizzaShop.controller.admin.AdminPizzaController
 * @see PizzaCreateDto
 * @see PizzaReadDto
 */
@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PizzaService {

    private final PizzaRepository pizzaRepository;
    private final IngredientRepository ingredientRepository;
    private final SizeTemplateRepository sizeTemplateRepository;
    private final PizzaFilterRepositoryImpl pizzaFilterRepository;

    private final PizzaCreateMapper pizzaCreateMapper;
    private final PizzaReadMapper pizzaReadMapper;
    private final PizzaUpdateMapper pizzaUpdateMapper;

    private final PriceCalculator priceCalculator;

    /**
     * Ищет пиццы по переданному фильтру.
     * @see PizzaFilter
     *
     * @param filter фильтр
     * @return список соответсвующих фильтру пицц.
     */
    public List<PizzaCardDto> getPizzaCardsByFilter(PizzaFilter filter) {
        return pizzaFilterRepository.findByFilter(filter);
    }

    /**
     * Ищет доступные пиццы и возвращает их в виде карточек (минимальная необходимая информация).
     * @see PizzaCardDto
     *
     * @param pageNumber номер слайса
     * @param pageSize размер слайса
     * @return слайс с карточками пицц
     */
    public Slice<PizzaCardDto> getPizzaCardsSlice(int pageNumber, int pageSize) {

        if(pageNumber < 0 || pageSize <= 0) throw new IllegalArgumentException("Некорректные данные для номера либо размера страницы");

        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        Slice<PizzaRepository.PizzaCardProjection> pizzasCardsSlice =
                pizzaRepository.findAvailablePizzasForCards(pageable);

        return pizzasCardsSlice.map(
                projection -> new PizzaCardDto(
                        projection.getId(),
                        projection.getName(),
                        projection.getDescription(),
                        projection.getImageUrl(),
                        projection.getCategory(),
                        projection.getStartPrice()
                ));
    }

    /**
     * Возвращает полную информацию о пицце с кэшированием результата.
     *
     * @param id идентификатор пиццы в БД
     * @return {@link PizzaCardDetailsDto} с полной информацией о пицце
     * @throws EntityNotFoundException если пицца не найдена
     *
     * @see PizzaCardDetailsDto
     */
    @Cacheable(value = "pizzaCardDetails", key = "#id")
    public PizzaCardDetailsDto getPizzaDetails(Long id) {
        log.info("Вызов getPizzaDetails с id={} (id==null? {})", id, id == null);

        Pizza pizza = pizzaRepository.findPizzaDetails(id).orElseThrow(() -> new EntityNotFoundException("Пицца с ID - "+ id + " не найдена в бд"));

        return new PizzaCardDetailsDto(
                id,
                pizza.getName(),
                pizza.getDescription(),
                pizza.getImageUrl(),
                pizza.getCategory(),
                pizza.getCookingTimeMinutes(),
                pizza.getPizzaIngredients().stream()
                        .map(element -> new IngredientInfoDto(
                                element.getIngredient().getName()
                        )).toList(),
                pizza.getPizzaSizes().stream().map(
                        element -> new PizzaSizeInfoDto(
                                new SizeTemplateInfoDto(
                                      element.getSizeTemplate().getSizeName(),
                                      element.getSizeTemplate().getDisplayName(),
                                      element.getSizeTemplate().getDiameterCm(),
                                      element.getSizeTemplate().getWeightGrams()
                                ),
                                element.getPrice()
                        )
                ).toList()
        );
    }

    /**
     * Создаёт новую пиццу в системе.
     * <p>
     * <b>Бизнес-правила:</b>
     * <ul>
     *   <li>Имя пиццы должно быть уникальным</li>
     *   <li>Пицца должна содержать минимум 1 ингредиент</li>
     *   <li>Должен быть указан хотя бы один размер</li>
     *   <li>Цена рассчитывается автоматически на основе ингредиентов</li>
     * </ul>
     * <p>
     * <b>Процесс создания:</b>
     * <ol>
     *   <li>Валидация входных данных</li>
     *   <li>Проверка уникальности имени</li>
     *   <li>Сохранение базовой информации о пицце</li>
     *   <li>Добавление ингредиентов с весами</li>
     *   <li>Расчёт цен для каждого размера</li>
     * </ol>
     *
     * @param createDto DTO с данными для создания пиццы
     * @return {@link PizzaReadDto} созданной пиццы
     * @throws PizzaAlreadyExistsException если пицца с таким именем уже существует
     * @throws EntityNotFoundException если не найдены ингредиенты или размеры
     * @throws IllegalArgumentException если пицца создаётся без ингредиентов или размеров
     *
     * @see #addIngredientsWithWeights(Pizza, Map, Map)
     * @see #addSizesToPizza(Pizza, List, Map, Map)
     */
    @Transactional
    public PizzaReadDto createPizza(PizzaCreateDto createDto) {

        log.info("Передано DTO на создание пиццы с именем {}", createDto.name());

        if (pizzaRepository.existsByName(createDto.name())) {
            log.warn("Пицца с именем \"{}\" уже существует в БД", createDto.name());
            throw new PizzaAlreadyExistsException("Пицца с таким именем уже существует");
        }

        Map<Long, Integer> pizzaIngredientsWeights = createDto.ingredientWeights();

        if(pizzaIngredientsWeights.isEmpty()) {
            log.warn("Попытка создать пиццу '{}' без ингредиентов", createDto.name());
            throw new IllegalArgumentException("Пицца должна содержать ингредиенты");
        }

        Set<Long> sizeTemplateIdsFromDto = createDto.sizeTemplateIds();

        if(sizeTemplateIdsFromDto.isEmpty()) {
            log.warn("Попытка создать пиццу '{}' без указанных размеров", createDto.name());
            throw new IllegalArgumentException("Для пиццы должны быть определены размеры");
        }

        Set<Long> ingredientIdsFromDto = pizzaIngredientsWeights.keySet();

        List<Ingredient> ingredientsFound = ingredientRepository.findAllById(ingredientIdsFromDto);

        if (ingredientsFound.size() != ingredientIdsFromDto.size()) {
            List<Long> ingredientIdsFound = ingredientsFound.stream()
                    .map(Ingredient::getId)
                    .toList();

            List<Long> ingredientIdsNotFound = ingredientIdsFromDto.stream()
                    .filter(el -> !ingredientIdsFound.contains(el))
                    .toList();

            if (!ingredientIdsNotFound.isEmpty()) {
                throw new EntityNotFoundException(
                        "Не найдены ингредиенты с id: " + ingredientIdsNotFound.stream()
                                .map(String::valueOf)
                                .collect(Collectors.joining(", "))
                );
            }
        }

        Map<Long, Ingredient> ingredientMap = ingredientsFound.stream()
                .collect(Collectors.toMap(Ingredient::getId, Function.identity()));

        List<SizeTemplate> foundSizeTemplates = sizeTemplateRepository.findAllById(sizeTemplateIdsFromDto);

        if (foundSizeTemplates.size() != sizeTemplateIdsFromDto.size()) {
            List<Long> sizesIdsFound = foundSizeTemplates.stream()
                    .map(SizeTemplate::getId)
                    .toList();

            List<Long> sizeTemplateIdsNotFound = sizeTemplateIdsFromDto.stream()
                    .filter(el -> !sizesIdsFound.contains(el))
                    .toList();

            if (!sizeTemplateIdsNotFound.isEmpty()) {
                throw new EntityNotFoundException(
                        "Не найдены размеры с id: " + sizeTemplateIdsNotFound.stream()
                                .map(String::valueOf)
                                .collect(Collectors.joining(", "))
                );
            }
        }

        Pizza pizza = pizzaCreateMapper.map(createDto); // Пицца без подвязанных ингредиентов

        Pizza savedPizza = pizzaRepository.save(pizza);
        log.debug("ID после сохранения пиццы: {}", pizza.getId());
        //pizzaRepository.flush();

        addIngredientsWithWeights(savedPizza, pizzaIngredientsWeights, ingredientMap);
        addSizesToPizza(savedPizza, foundSizeTemplates, ingredientMap, pizzaIngredientsWeights);

        log.info("Пицца \"{}\" сохранена в БД, количество переданных ингредиентов - {}", createDto.name(), ingredientsFound.size());

        return pizzaReadMapper.map(savedPizza);
    }

    /**
     * Добавляет ингредиенты с весами к пицце.
     * Используется только в {@link #createPizza(PizzaCreateDto)}.
     *
     * @param pizza пицца для добавления ингредиентов
     * @param ingredientWeights карта [ID ингредиента → вес в граммах]
     * @param ingredientMap карта [ID ингредиента → сущность {@link Ingredient}]
     */
    private void addIngredientsWithWeights(Pizza pizza,
                                           Map<Long, Integer> ingredientWeights,
                                           Map<Long, Ingredient> ingredientMap) {

        log.debug("Добавление ингредиентов к пицце '{}', всего: {}",
                pizza.getName(), ingredientWeights.size());

        int addedCount = 0;

        for(Map.Entry<Long, Integer> entry : ingredientWeights.entrySet()) {

            Long currentIngredientId = entry.getKey();
            Integer currentIngredientWeight = entry.getValue();

            PizzaIngredient pizzaIngredient = new PizzaIngredient();

            pizzaIngredient.setPizza(pizza);

            Ingredient ingredient = ingredientMap.get(currentIngredientId);

            pizzaIngredient.setIngredient(ingredient);
            pizzaIngredient.setWeightGrams(currentIngredientWeight);

            // Пицца сохраняется каскадно благодаря CascadeType.PERSIST
            // pizzaIngredientRepository.save() не требуется
            pizza.getPizzaIngredients().add(pizzaIngredient);

            addedCount++;

            log.trace("Добавлен ингредиент: {} (id={}), вес: {}г",
                    ingredient.getName(), currentIngredientId, currentIngredientWeight);

        }
        log.debug("Добавлено {} ингредиентов к пицце '{}'",
                addedCount, pizza.getName());
    }

    /**
     * Добавляет размеры к пицце с расчетом цены под конкретный размер, используя {@link PriceCalculator}.
     * Используется только в {@link #createPizza(PizzaCreateDto)}.
     *
     * @param pizza пицца для добавления ингредиентов
     * @param foundSizeTemplates найденные шаблоны размеров
     * @param ingredientMap карта [ID ингредиента → сущность {@link Ingredient}]
     * @param ingredientWeights карта [ID ингредиента → вес в граммах]
     * @throws PriceCalculationException если произошла ошибка в калькуляторе
     */
    private void  addSizesToPizza(Pizza pizza,
                                  List<SizeTemplate> foundSizeTemplates,
                                  Map<Long, Ingredient> ingredientMap,
                                  Map<Long, Integer> ingredientWeights) {

        log.debug("Добавление размеров к пицце '{}', шаблонов: {}",
                pizza.getName(), foundSizeTemplates.size());

        for (SizeTemplate template : foundSizeTemplates) {
            try {

                BigDecimal price = priceCalculator.calculatePrice(template, ingredientMap, ingredientWeights);

                log.debug("Рассчитана цена для размера '{}': {} руб.",
                        template.getDisplayName(), price);

                PizzaSize pizzaSize = PizzaSize.create(pizza, template, price, true);

                pizza.getPizzaSizes().add(pizzaSize);

                log.trace("Создан размер: {} ({}), цена: {} руб.",
                        template.getSizeName(), template.getDisplayName(), price);

            } catch (IllegalArgumentException | ArithmeticException e) {
                log.error("Ошибка расчета цены для шаблона '{}' пиццы '{}': {}",
                        template.getDisplayName(), pizza.getName(), e.getMessage());
                throw new PriceCalculationException(
                        "Не удалось рассчитать цену для размера " + template.getDisplayName(), e);
            }
        }

        log.debug("Добавлено {} размеров к пицце '{}'",
                foundSizeTemplates.size(), pizza.getName());
    }

    /**
     * Обновляет базовую информацию о пицце.
     *
     * @param id идентификатор обновляемой пиццы
     * @param updateDto DTO для обновления пиццы
     * @return DTO, содержащее обновлённую о пицце информацию
     * @throws PizzaNotFoundException если пицца не найдена по переданному идентификатору
     * @throws PizzaAlreadyExistsException если новое имя пиццы уже существует в БД
     */
    @Transactional
    @CacheEvict(value = "pizzaCardDetails", key = "#id")
    public PizzaUpdateResponseDto updatePizzaById(Long id, PizzaUpdateDto updateDto) {

        log.info("Передано DTO на обновление пиццы. Имя для обновления - \"{}\", id - {}", updateDto.name(), id);


        Pizza pizzaToUpdate = pizzaRepository.findById(id).orElseThrow(() -> new PizzaNotFoundException(
                String.format("Пицца для обновления с id %d не найдена в БД", id)));


        if(pizzaRepository.existsByName(updateDto.name())) {
            throw new PizzaAlreadyExistsException("Пицца с именем " + updateDto.name() + " уже существует!");
        }

        pizzaUpdateMapper.map(updateDto,pizzaToUpdate);

        Pizza updated = pizzaRepository.save(pizzaToUpdate);
        log.info("Пицца с id {} успешно обновлена", id);

        return new PizzaUpdateResponseDto(
                pizzaToUpdate.getId(),
                pizzaToUpdate.getName(),
                pizzaToUpdate.getDescription(),
                pizzaToUpdate.getImageUrl(),
                pizzaToUpdate.getCategory(),
                pizzaToUpdate.isAvailable(),
                pizzaToUpdate.getCookingTimeMinutes(),
                LocalDateTime.now()
        );
    }

    @Transactional
    @CacheEvict(value = "pizzaCardDetails", key = "#pizzaId")
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