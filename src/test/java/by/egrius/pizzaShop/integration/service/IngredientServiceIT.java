package by.egrius.pizzaShop.integration.service;

import by.egrius.pizzaShop.dto.ingredient.IngredientCreateDto;
import by.egrius.pizzaShop.dto.ingredient.IngredientReadDto;
import by.egrius.pizzaShop.dto.ingredient.IngredientUpdateDto;
import by.egrius.pizzaShop.dto.pizza.PizzaCreateDto;
import by.egrius.pizzaShop.entity.*;
import by.egrius.pizzaShop.integration.testcontainer.TestContainerBase;
import by.egrius.pizzaShop.repository.*;
import by.egrius.pizzaShop.service.IngredientService;
import by.egrius.pizzaShop.service.PizzaService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Sql(scripts = "/test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class IngredientServiceIT extends TestContainerBase {

    @Autowired
    IngredientService ingredientService;

    @Autowired
    IngredientRepository ingredientRepository;

    @Nested
    class createIngredientTest {

        @Test
        void createIngredient_success() {

            assertThat(ingredientRepository.count())
                    .as("БД должна быть пустой перед тестом")
                    .isEqualTo(0);

            IngredientCreateDto dto = new IngredientCreateDto(
                    "Сыр", "Вкусный сыр", new BigDecimal("10.0"), true
            );

            IngredientReadDto result = ingredientService.createIngredient(dto);

            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("Сыр");
            assertThat(ingredientRepository.count())
                    .as("После создания должен быть 1 ингредиент")
                    .isEqualTo(1);
        }

        @Test
        void createIngredient_shouldThrowAndNotCreateIfNameAlreadyExists() {
            Ingredient existing = Ingredient.create("Сыр", "Вкусный сыр", BigDecimal.valueOf(10.5), true);
            ingredientRepository.save(existing);

            IngredientCreateDto ingredientCreateDto = new IngredientCreateDto(
                    "Сыр",
                    "Невкусный сыр",
                    BigDecimal.valueOf(9.15),
                    true
            );

            assertThatThrownBy(() -> ingredientService.createIngredient(ingredientCreateDto))
                    .hasMessageContaining("уже существует в БД");

            assertEquals(ingredientRepository.countByName("Сыр"), 1L);
        }
    }

    @Nested
    class GetIngredientsTest {

        @BeforeEach
        void setup() {
            ingredientRepository.save(Ingredient.create("Сыр", "Вкусный сыр", new BigDecimal("10.0"), true));
            ingredientRepository.save(Ingredient.create("Ветчина", "Вкусная ветчина", new BigDecimal("18.0"), true));
            ingredientRepository.save(Ingredient.create("Грибы", "Вкусные грибы", new BigDecimal("12.7"), true));
            ingredientRepository.save(Ingredient.create("Лук красный", "Вкусный красный лук", new BigDecimal("9.7"), true));
            ingredientRepository.save(Ingredient.create("Огурцы", "Вкусные огурцы", new BigDecimal("9.4"), true));

        }

        @Test
        void getAllIngredients_shouldReturnPageOfIngredients() {
            Pageable pageable_1 = PageRequest.of(0,3);

            Page<IngredientReadDto> pageResult_1 = ingredientService.getAllIngredients(0,3);

            assertNotNull(pageResult_1);
            List<IngredientReadDto> pageResult_1Content = pageResult_1.getContent();

            assertEquals(pageResult_1Content.size(), 3);

            assertEquals(pageResult_1Content.get(0).id(), 1L);
            assertEquals(pageResult_1Content.get(0).name(), "Сыр");

            assertEquals(pageResult_1Content.get(1).id(), 2L);
            assertEquals(pageResult_1Content.get(1).name(), "Ветчина");

            assertEquals(pageResult_1Content.get(2).id(), 3L);
            assertEquals(pageResult_1Content.get(2).name(), "Грибы");

            Pageable pageable_2 = PageRequest.of(1,3);

            Page<IngredientReadDto> pageResult_2 = ingredientService.getAllIngredients(1,3);

            assertNotNull(pageResult_2);
            List<IngredientReadDto> pageResult_2Content = pageResult_2.getContent();

            assertEquals(pageResult_2Content.size(), 2);

            assertEquals(pageResult_2Content.get(0).id(), 4L);
            assertEquals(pageResult_2Content.get(0).name(), "Лук красный");

            assertEquals(pageResult_2Content.get(1).id(), 5L);
            assertEquals(pageResult_2Content.get(1).name(), "Огурцы");
        }

        @Test
        void getIngredientsByIds_shouldReturnRelevantEntities() {
            List<IngredientReadDto> results = ingredientService.getIngredientsByIds(Set.of(1L, 2L));

            assertEquals(results.get(0).id(), 1L);
            assertEquals(results.get(0).name(), "Сыр");

            assertEquals(results.get(1).id(), 2L);
            assertEquals(results.get(1).name(), "Ветчина");
        }

        @Test
        void getIngredientsByIds_shouldThrowWhenNoIds() {
            assertThatThrownBy(() -> ingredientService.getIngredientsByIds(Set.of()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Список ID ингредиентов не может быть пустым");

        }

        @Test
        void getIngredientsByIds_shouldThrowWhenNotAllEntitiesFound() {
            assertThatThrownBy(() -> ingredientService.getIngredientsByIds(Set.of(1L, 2L, 999L)))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Не найдены ингредиенты с ID: [999] (запрошено: 3, найдено: 2)");

        }
    }

    @Nested
    class UpdateIngredientTest {

        @Autowired
        SizeTemplateRepository sizeTemplateRepository;

        @Autowired
        PizzaIngredientRepository pizzaIngredientRepository;

        @Autowired
        PizzaSizeRepository pizzaSizeRepository;

        @Autowired
        PizzaService pizzaService;

        @BeforeEach
        void setup() {
            ingredientRepository.save(Ingredient.create("Сыр", "Вкусный сыр", new BigDecimal("10.0"), true));
            ingredientRepository.save(Ingredient.create("Ветчина", "Вкусная ветчина", new BigDecimal("18.0"), true));
            ingredientRepository.save(Ingredient.create("Грибы", "Вкусные грибы", new BigDecimal("12.7"), true));
            ingredientRepository.save(Ingredient.create("Лук красный", "Вкусный красный лук", new BigDecimal("9.7"), true));
            ingredientRepository.save(Ingredient.create("Огурцы", "Вкусные огурцы", new BigDecimal("9.4"), true));

            sizeTemplateRepository.save(SizeTemplate.create(PizzaSizeEnum.SMALL, PizzaSizeEnum.SMALL.getAbbreviation(), 25, 450, BigDecimal.ONE));
            sizeTemplateRepository.save(SizeTemplate.create(PizzaSizeEnum.MEDIUM, PizzaSizeEnum.MEDIUM.getAbbreviation(), 30, 650, BigDecimal.ONE));
            sizeTemplateRepository.save(SizeTemplate.create(PizzaSizeEnum.LARGE, PizzaSizeEnum.LARGE.getAbbreviation(), 35, 700, BigDecimal.ONE));

            pizzaService.createPizza(new PizzaCreateDto(
                    "Маргарита",
                    "Классическая пицца",
                    "/images/margherita.jpg",
                    "Классические",
                    true,
                    15,
                    Map.of(1L, 200, 2L, 250, 3L, 40), // ingredientId → weightGrams
                    Set.of(1L, 2L, 3L)
            ));
        }

        @Test
        void shouldUpdateIngredientAndChangesShouldBeVisible() {
            ingredientService.updateIngredient(1L, new IngredientUpdateDto(
                    "НОВЫЙ ИНГРЕДИЕНТ",
                    "НОВОЕ ОПИСАНИЕ",
                    BigDecimal.valueOf(999.99),
                    true
            ));

            Optional<Ingredient> sameAfterUpdate = ingredientRepository.findById(1L);
            assertTrue(sameAfterUpdate.isPresent());

            assertEquals(sameAfterUpdate.get().getName(), "НОВЫЙ ИНГРЕДИЕНТ");
            assertEquals(sameAfterUpdate.get().getDescription(), "НОВОЕ ОПИСАНИЕ");
            assertEquals(sameAfterUpdate.get().getPrice(), BigDecimal.valueOf(999.99));
            assertTrue(sameAfterUpdate.get().isAvailable());

            List<PizzaIngredient> pizzaIngredientsList = pizzaIngredientRepository.findByPizzaId(1L);
            assertFalse(pizzaIngredientsList.isEmpty());

            Long ingredientIdInPizza = pizzaIngredientsList.get(0).getIngredient().getId();
            Ingredient ingredientFromPizza = ingredientRepository.findById(ingredientIdInPizza).orElseThrow();

            assertEquals("НОВЫЙ ИНГРЕДИЕНТ", ingredientFromPizza.getName());
        }

        @Test
        void updateIngredientPrice_shouldRecalculatePizzaPrice() throws InterruptedException {

            List<PizzaSize> pizzaSize = pizzaSizeRepository.findByPizzaId(1L);

            assertFalse(pizzaSize.isEmpty());

            BigDecimal priceBefore_1 = pizzaSize.get(0).getPrice();
            BigDecimal priceBefore_2 = pizzaSize.get(1).getPrice();
            BigDecimal priceBefore_3 = pizzaSize.get(2).getPrice();

            // Arrange
            BigDecimal newPrice = BigDecimal.valueOf(999.99);

            // Act
            ingredientService.updateIngredient(1L, new IngredientUpdateDto(
                    null, null, newPrice, null
            ));

            Thread.sleep(4000);

            List<PizzaSize> pizzaSizesAfter = pizzaSizeRepository.findByPizzaId(1L);

            BigDecimal priceAfter_1 = pizzaSizesAfter.get(0).getPrice();
            BigDecimal priceAfter_2 = pizzaSizesAfter.get(1).getPrice();
            BigDecimal priceAfter_3 = pizzaSizesAfter.get(2).getPrice();

            assertNotEquals(priceBefore_1, priceAfter_1);
            assertNotEquals(priceBefore_2, priceAfter_2);
            assertNotEquals(priceBefore_3, priceAfter_3);
        }
    }

    @Nested
    class  DeleteIngredientTest {
        @Autowired
        SizeTemplateRepository sizeTemplateRepository;

        @Autowired
        PizzaIngredientRepository pizzaIngredientRepository;

        @Autowired
        PizzaService pizzaService;

        @BeforeEach
        void setup() {
            ingredientRepository.save(Ingredient.create("Сыр", "Вкусный сыр", new BigDecimal("10.0"), true));
            ingredientRepository.save(Ingredient.create("Ветчина", "Вкусная ветчина", new BigDecimal("18.0"), true));
            ingredientRepository.save(Ingredient.create("Грибы", "Вкусные грибы", new BigDecimal("12.7"), true));
            ingredientRepository.save(Ingredient.create("Лук красный", "Вкусный красный лук", new BigDecimal("9.7"), true));

            sizeTemplateRepository.save(SizeTemplate.create(PizzaSizeEnum.MEDIUM, "30см", 30, 650, BigDecimal.ONE));

            pizzaService.createPizza(new PizzaCreateDto(
                    "Маргарита",
                    "Классическая пицца",
                    "/images/margherita.jpg",
                    "Классические",
                    true,
                    15,
                    Map.of(1L, 200, 2L, 250, 3L, 40), // ingredientId → weightGrams
                    Set.of(1L)
            ));
        }

        @Test
        void deleteIngredient_shouldDeleteIfIngredientIsNotLinked() {
            ingredientService.deleteIngredient(4L);

            assertFalse(ingredientRepository.findById(4L).isPresent());
        }

        @Test
        void deleteIngredient_shouldNotDeleteIfItHasBeenLinkedToPizza() {
            assertThatThrownBy(() -> ingredientService.deleteIngredient(1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Невозможно удалить ингредиент 'Сыр', так как он используется в 1 пиццах");
            assertTrue(ingredientRepository.existsById(1L));
            assertEquals("Сыр", ingredientRepository.findById(1L).orElseThrow().getName());
        }

    }
}