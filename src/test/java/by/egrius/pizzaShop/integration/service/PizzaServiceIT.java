package by.egrius.pizzaShop.integration.service;

import by.egrius.pizzaShop.dto.ingredient.IngredientInfoDto;
import by.egrius.pizzaShop.dto.pizza.*;
import by.egrius.pizzaShop.dto.pizza_size.PizzaSizeInfoDto;
import by.egrius.pizzaShop.dto.size_template.SizeTemplateInfoDto;
import by.egrius.pizzaShop.entity.Ingredient;
import by.egrius.pizzaShop.entity.Pizza;
import by.egrius.pizzaShop.entity.PizzaSizeEnum;
import by.egrius.pizzaShop.entity.SizeTemplate;
import by.egrius.pizzaShop.repository.*;
import by.egrius.pizzaShop.service.PizzaService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Slice;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Sql(scripts = "/test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class PizzaServiceIT {

    @Autowired
    PizzaService pizzaService;

    @Autowired
    IngredientRepository ingredientRepository;

    @Autowired
    PizzaRepository pizzaRepository;

    @Autowired
    SizeTemplateRepository sizeTemplateRepo;

    @Nested
    class CreateTests {

        @BeforeEach
        void setUp() {

            ingredientRepository.save(Ingredient.create("Моцарелла", "...", new BigDecimal("6.64"), true));
            ingredientRepository.save(Ingredient.create("Цыплёнок", "...", new BigDecimal("10.0"), true));
            ingredientRepository.save(Ingredient.create("Бекон", "...", new BigDecimal("1.60"), true));

            sizeTemplateRepo.save(SizeTemplate.create(PizzaSizeEnum.SMALL, "25см", 25, 450, new BigDecimal("0.8")));
            sizeTemplateRepo.save(SizeTemplate.create(PizzaSizeEnum.MEDIUM, "30см", 30, 650, BigDecimal.ONE));
        }

        @Test
        void shouldCreatePizzaWithAllData() {
            // Given
            PizzaCreateDto dto = new PizzaCreateDto(
                    "Маргарита",
                    "Классическая пицца",
                    "/images/margherita.jpg",
                    "Классические",
                    true,
                    15,
                    Map.of(1L, 200, 2L, 250, 3L, 40), // ingredientId → weightGrams
                    Set.of(1L, 2L) // sizeTemplateIds (SMALL, MEDIUM)
            );

            PizzaReadDto result = pizzaService.createPizza(dto);

            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("Маргарита");
            assertThat(result.pizzaIngredientReadDtos()).hasSize(3);
            assertThat(result.pizzaSizeReadDtos()).hasSize(2);

            assertThat(result.pizzaSizeReadDtos().get(0).price()).isPositive();
            assertThat(result.pizzaSizeReadDtos().get(1).price()).isPositive();

            System.out.println(result.pizzaSizeReadDtos().get(0).price());
            System.out.println(result.pizzaSizeReadDtos().get(1).price());

            // SMALL должен быть дешевле MEDIUM
            assertThat(result.pizzaSizeReadDtos().get(0).price())
                    .isLessThan(result.pizzaSizeReadDtos().get(1).price());
        }
    }

    @Nested
    class GetPizzaCardsTests {

        @BeforeEach
        void setup () {
            ingredientRepository.save(Ingredient.create("Моцарелла", "...", new BigDecimal("6.64"), true));
            ingredientRepository.save(Ingredient.create("Цыплёнок", "...", new BigDecimal("10.0"), true));
            ingredientRepository.save(Ingredient.create("Бекон", "...", new BigDecimal("1.60"), true));

            sizeTemplateRepo.save(SizeTemplate.create(PizzaSizeEnum.SMALL, "25см", 25, 450, new BigDecimal("0.8")));
            sizeTemplateRepo.save(SizeTemplate.create(PizzaSizeEnum.MEDIUM, "30см", 30, 650, BigDecimal.ONE));

            PizzaCreateDto dto1= new PizzaCreateDto(
                    "Маргарита",
                    "Классическая пицца",
                    "/images/margherita.jpg",
                    "Классические",
                    true,
                    15,
                    Map.of(1L, 200, 2L, 250, 3L, 40), // ingredientId -> weightGrams
                    Set.of(1L, 2L) // sizeTemplateIds (SMALL, MEDIUM)
            );

            PizzaCreateDto dto2= new PizzaCreateDto(
                    "Пицца_2",
                    "Классическая пицца_2",
                    "/images/margherita.jpg",
                    "Классические",
                    true,
                    15,
                    Map.of(1L, 200, 2L, 250), // ingredientId -> weightGrams
                    Set.of(1L, 2L) // sizeTemplateIds (SMALL, MEDIUM)
            );

            pizzaService.createPizza(dto1);
            pizzaService.createPizza(dto2);

        }

        @Test
        void shouldReturnPizzaSlices() {
            Slice<PizzaCardDto> result = pizzaService.getPizzaCardsSlice(0, 5);

            List<PizzaCardDto> pizzaCards = result.getContent();

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.hasNext()).isFalse();

            PizzaCardDto first = result.getContent().get(0);
            assertThat(first.id()).isNotNull();
            assertThat(first.name()).isIn("Маргарита", "Пицца_2");
            assertThat(first.startPrice()).isPositive();

            pizzaCards.forEach(System.out::println);
        }

        @Test
        void shouldReturnCorrectPagination() {
            for (int i = 3; i <= 15; i++) {
                pizzaService.createPizza(new PizzaCreateDto(
                        "Пицца_" + i,
                        "Классическая пицца",
                        "/images/margherita.jpg",
                        "Классические",
                        true,
                        15,
                        Map.of(1L, 200, 2L, 250, 3L, 40),
                        Set.of(1L, 2L)
                ));
            }

            Slice<PizzaCardDto> page1 = pizzaService.getPizzaCardsSlice(0, 5);
            assertThat(page1.getContent()).hasSize(5);
            assertThat(page1.hasNext()).isTrue();

            Slice<PizzaCardDto> page2 = pizzaService.getPizzaCardsSlice(1, 5);
            assertThat(page2.getContent()).hasSize(5);
            assertThat(page2.hasNext()).isTrue();

            Slice<PizzaCardDto> page3 = pizzaService.getPizzaCardsSlice(2, 5);
            assertThat(page3.getContent()).hasSize(5);
            assertThat(page3.hasNext()).isFalse();
        }

        @Test
        void shouldNotReturnUnavailableDto() {
            PizzaCreateDto unavailable = new PizzaCreateDto(
                    "Недоступная",
                    "Классическая пицца_2",
                    "/images/margherita.jpg",
                    "Классические",
                    false,
                    15,
                    Map.of(1L, 200, 2L, 250), // ingredientId -> weightGrams
                    Set.of(1L, 2L) // sizeTemplateIds (SMALL, MEDIUM)
            );

            pizzaService.createPizza(unavailable);

            Slice<PizzaCardDto> page = pizzaService.getPizzaCardsSlice(0, 5);
            assertNotNull(page.getContent());

            boolean unavailableFound = page.getContent().stream().anyMatch(
                    dto -> dto.name().equals("Недоступная")
            );

            assertFalse(unavailableFound);
        }
    }

    @Nested
    class GetPizzaDetailsTests {

        private Long createdPizzaId;

        @BeforeEach
        void setUp() {
            // Создаем тестовые данные
            ingredientRepository.save(Ingredient.create("Моцарелла", "...", new BigDecimal("25.00"), true));
            ingredientRepository.save(Ingredient.create("Помидоры", "...", new BigDecimal("12.00"), true));

            sizeTemplateRepo.save(SizeTemplate.create(PizzaSizeEnum.SMALL, "25 см", 25, 450, new BigDecimal("0.8")));
            sizeTemplateRepo.save(SizeTemplate.create(PizzaSizeEnum.MEDIUM, "30 см", 30, 650, BigDecimal.ONE));

            // Создаем пиццу
            PizzaCreateDto createDto = new PizzaCreateDto(
                    "Тестовая пицца",
                    "Описание тестовой пиццы",
                    "/images/test.jpg",
                    "Тестовые",
                    true,
                    15,
                    Map.of(1L, 200, 2L, 150), // Ингредиенты
                    Set.of(1L, 2L) // Размеры
            );

            PizzaReadDto created = pizzaService.createPizza(createDto);
            createdPizzaId = created.id();
        }

        @Test
        @Transactional
        void testPizzaDetailsLoading() {
            Pizza pizza = pizzaRepository.findPizzaDetails(createdPizzaId).orElseThrow();

            // Проверяем что пицца загружена
            assertThat(pizza).isNotNull();
            assertThat(pizza.getName()).isEqualTo("Тестовая пицца");

            // Проверяем ингредиенты
            assertThat(pizza.getPizzaIngredients())
                    .isNotEmpty()
                    .allMatch(pi -> pi.getIngredient() != null);

            // Проверяем размеры
            assertThat(pizza.getPizzaSizes())
                    .isNotEmpty()
                    .allMatch(ps -> {
                        assertThat(ps.getPrice()).isNotNull().isPositive();
                        assertThat(ps.getSizeTemplate()).isNotNull();
                        assertThat(ps.getSizeTemplate().getDisplayName()).isNotBlank();
                        return true;
                    });

            System.out.println("Ингредиенты: " + pizza.getPizzaIngredients().size());
            System.out.println("Размеры: " + pizza.getPizzaSizes().size());
            pizza.getPizzaSizes().forEach(ps ->
                    System.out.println("Цена: " + ps.getPrice() + ", Размер: " + ps.getSizeTemplate().getDisplayName())
            );
        }

        @Test
        void shouldReturnPizzaDetails_whenPizzaExists() {
            // When
            PizzaCardDetailsDto result = pizzaService.getPizzaDetails(createdPizzaId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(createdPizzaId);
            assertThat(result.name()).isEqualTo("Тестовая пицца");
            assertThat(result.description()).isEqualTo("Описание тестовой пиццы");
            assertThat(result.imageUrl()).isEqualTo("/images/test.jpg");
            assertThat(result.category()).isEqualTo("Тестовые");
            assertThat(result.cookingTimeMinutes()).isEqualTo(15);

            System.out.println(result);

            // Проверяем ингредиенты
            assertThat(result.ingredientInfoDtos())
                    .hasSize(2)
                    .extracting(IngredientInfoDto::name)
                    .containsExactlyInAnyOrder("Моцарелла", "Помидоры");

            // Проверяем размеры
            assertThat(result.pizzaSizeInfoDtos())
                    .hasSize(2)
                    .extracting(PizzaSizeInfoDto::sizeTemplateInfoDto)
                    .extracting(SizeTemplateInfoDto::displayName)
                    .containsExactlyInAnyOrder("25 см", "30 см");

            // Проверяем что цены рассчитались
            result.pizzaSizeInfoDtos().forEach(size -> {
                assertThat(size.price())
                        .isPositive()
                        .isGreaterThan(BigDecimal.ZERO);
            });

            // SMALL должен быть дешевле MEDIUM
            BigDecimal smallPrice = result.pizzaSizeInfoDtos().stream()
                    .filter(s -> s.sizeTemplateInfoDto().sizeName() == PizzaSizeEnum.SMALL)
                    .findFirst()
                    .map(PizzaSizeInfoDto::price)
                    .orElseThrow();

            BigDecimal mediumPrice = result.pizzaSizeInfoDtos().stream()
                    .filter(s -> s.sizeTemplateInfoDto().sizeName() == PizzaSizeEnum.MEDIUM)
                    .findFirst()
                    .map(PizzaSizeInfoDto::price)
                    .orElseThrow();

            assertThat(smallPrice).isLessThan(mediumPrice);
        }

        @Test
        void shouldThrow_whenPizzaNotFound() {

        }
    }

    @Nested
    class UpdatePizzaTests {

        private Long createdPizzaId;

        @BeforeEach
        void setUp() {
            // Создаем тестовые данные
            ingredientRepository.save(Ingredient.create("Моцарелла", "...", new BigDecimal("25.00"), true));
            ingredientRepository.save(Ingredient.create("Помидоры", "...", new BigDecimal("12.00"), true));

            sizeTemplateRepo.save(SizeTemplate.create(PizzaSizeEnum.SMALL, "25 см", 25, 450, new BigDecimal("0.8")));
            sizeTemplateRepo.save(SizeTemplate.create(PizzaSizeEnum.MEDIUM, "30 см", 30, 650, BigDecimal.ONE));

            // Создаем пиццу
            PizzaCreateDto createDto = new PizzaCreateDto(
                    "Тестовая пицца",
                    "Описание тестовой пиццы",
                    "/images/test.jpg",
                    "Тестовые",
                    true,
                    15,
                    Map.of(1L, 200, 2L, 150), // Ингредиенты
                    Set.of(1L, 2L) // Размеры
            );

            PizzaReadDto created = pizzaService.createPizza(createDto);
            createdPizzaId = created.id();
        }

        @Test
        void shouldUpdatePizzaInfoCorrect() {
            PizzaUpdateDto pizzaUpdateDto = new PizzaUpdateDto(
                    "Обновлённое имя",
                    "Обновлённое описание",
                    "/new_url/image.png",
                    "Новая категория",
                    true,
                    20
            );

            PizzaUpdateResponseDto response = pizzaService.updatePizzaById(createdPizzaId, pizzaUpdateDto);

            assertEquals("Обновлённое имя", response.name());
            assertEquals("Обновлённое описание", response.description());
            assertEquals("/new_url/image.png", response.imageUrl());
            assertEquals("Новая категория", response.category());
            assertTrue(response.isAvailable());
            assertEquals(20, response.cookingTimeMinutes());

            Optional<Pizza> afterUpdatePizza = pizzaRepository.findById(createdPizzaId);
            assertTrue(afterUpdatePizza.isPresent());
            assertEquals("Обновлённое имя", afterUpdatePizza.get().getName());
            assertEquals(20, afterUpdatePizza.get().getCookingTimeMinutes());
        }

        @Test
        void shouldThrowException_whenUpdatingNonExistentPizza() {
            Long nonExistentId = 999L;
            PizzaUpdateDto updateDto = new PizzaUpdateDto("Новое имя", "Описание", "/img.jpg", "Категория", true, 20
            );

            EntityNotFoundException exception = assertThrows(
                    EntityNotFoundException.class,
                    () -> pizzaService.updatePizzaById(nonExistentId, updateDto)
            );

            assertTrue(exception.getMessage().contains(String.valueOf(nonExistentId)));
        }

        @Test
        void shouldHandleNullDtoGracefully() {
            assertThrows(NullPointerException.class,
                    () -> pizzaService.updatePizzaById(createdPizzaId, null));
        }

        @Test
        void shouldUpdateOnlyName_whenOtherFieldsAreNull() {
            PizzaUpdateDto updateDto = new PizzaUpdateDto(
                    "Только имя изменено",
                    null,
                    null,
                    null,
                    null,
                    null
            );

            PizzaUpdateResponseDto response = pizzaService.updatePizzaById(
                    createdPizzaId, updateDto);

            assertEquals("Только имя изменено", response.name());
            // Остальные поля должны остаться как были
            assertTrue(response.isAvailable());
            assertEquals(15, response.cookingTimeMinutes());  // Исходное значение
        }

        @Test
        void shouldUpdateOnlyAvailabilityToFalse() {
            PizzaUpdateDto updateDto = new PizzaUpdateDto(
            null, null, null, null, false, null
            );

            PizzaUpdateResponseDto response = pizzaService.updatePizzaById(
                    createdPizzaId, updateDto);

            assertFalse(response.isAvailable());
            assertEquals("Тестовая пицца", response.name());
        }

        @Test
        void shouldUpdateMultipleFieldsButNotAll() {
            PizzaUpdateDto updateDto = new PizzaUpdateDto(
                    "Новое имя",
                    "Новое описание",
                    null,
                    "Новая категория",
                    false,
                    null
            );

            PizzaUpdateResponseDto response = pizzaService.updatePizzaById(
                    createdPizzaId, updateDto);

            assertEquals("Новое имя", response.name());
            assertEquals("Новое описание", response.description());
            assertEquals("Новая категория", response.category());
            assertFalse(response.isAvailable());
            assertEquals("/images/test.jpg", response.imageUrl());
            assertEquals(15, response.cookingTimeMinutes());
        }

        @Test
        void shouldThrowOptimisticLockException_whenConcurrentUpdate() {
            // Создаем две копии одной пиццы в памяти
            Pizza copy1 = pizzaRepository.findById(createdPizzaId).orElseThrow();
            Pizza copy2 = pizzaRepository.findById(createdPizzaId).orElseThrow();

            // Обновляем первую копию
            copy1.setName("Копия 1");
            pizzaRepository.save(copy1);

            // Пытаемся обновить вторую копию (та же версия)
            copy2.setName("Копия 2");

            assertThrows(ObjectOptimisticLockingFailureException.class, () -> {
                pizzaRepository.save(copy2);
            });
        }

        @Test
        void shouldUpdateVersionField() {
            Pizza original = pizzaRepository.findById(createdPizzaId).orElseThrow();
            Long originalVersion = original.getVersion();

            PizzaUpdateDto updateDto = new PizzaUpdateDto(
                     "Обновленная", null, null, null, null, null
            );

            pizzaService.updatePizzaById(createdPizzaId, updateDto);

            Pizza updated = pizzaRepository.findById(createdPizzaId).orElseThrow();
            assertEquals(originalVersion + 1, updated.getVersion());
        }

    }

    @Nested
    class DeletePizzaTests {

        private Long createdPizzaId;

        @Autowired
        PizzaIngredientRepository pizzaIngredientRepository;

        @Autowired
        PizzaSizeRepository pizzaSizeRepository;

        @BeforeEach
        void setUp() {
            // Создаем тестовые данные
            ingredientRepository.save(Ingredient.create("Моцарелла", "...", new BigDecimal("25.00"), true));
            ingredientRepository.save(Ingredient.create("Помидоры", "...", new BigDecimal("12.00"), true));

            sizeTemplateRepo.save(SizeTemplate.create(PizzaSizeEnum.SMALL, "25 см", 25, 450, new BigDecimal("0.8")));
            sizeTemplateRepo.save(SizeTemplate.create(PizzaSizeEnum.MEDIUM, "30 см", 30, 650, BigDecimal.ONE));

            // Создаем пиццу
            PizzaCreateDto createDto = new PizzaCreateDto(
                    "Тестовая пицца",
                    "Описание тестовой пиццы",
                    "/images/test.jpg",
                    "Тестовые",
                    true,
                    15,
                    Map.of(1L, 200, 2L, 150),
                    Set.of(1L, 2L)
            );

            PizzaReadDto created = pizzaService.createPizza(createDto);
            createdPizzaId = created.id();
        }

        @Test
        void shouldDeletePizzaDespiteIngredients_whenPizzaFound() {
            pizzaService.deletePizzaById(1L);

            assertFalse(pizzaRepository.findById(1L).isPresent());

            Optional<Ingredient> ingredient1 = ingredientRepository.findById(1L);
            assertTrue(ingredient1.isPresent());
            assertEquals(ingredient1.get().getName(), "Моцарелла");

            Optional<Ingredient> ingredient2 = ingredientRepository.findById(2L);
            assertTrue(ingredient2.isPresent());
            assertEquals(ingredient2.get().getName(), "Помидоры");

            Optional<SizeTemplate> sizeTemplate1 = sizeTemplateRepo.findById(1L);
            assertTrue(sizeTemplate1.isPresent());
            assertEquals(sizeTemplate1.get().getSizeName(), PizzaSizeEnum.SMALL);


            Optional<SizeTemplate> sizeTemplate2 = sizeTemplateRepo.findById(2L);
            assertTrue(sizeTemplate2.isPresent());
            assertEquals(sizeTemplate2.get().getSizeName(), PizzaSizeEnum.MEDIUM);

            assertTrue(pizzaIngredientRepository.findByPizzaId(1L).isEmpty());
            assertTrue(pizzaSizeRepository.findByPizzaId(1L).isEmpty());
        }

    }
}