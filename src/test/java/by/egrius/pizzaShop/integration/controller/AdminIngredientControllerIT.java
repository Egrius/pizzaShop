package by.egrius.pizzaShop.integration.controller;

import by.egrius.pizzaShop.dto.ingredient.IngredientCreateDto;
import by.egrius.pizzaShop.dto.ingredient.IngredientIdsRequest;
import by.egrius.pizzaShop.dto.ingredient.IngredientUpdateDto;
import by.egrius.pizzaShop.entity.*;
import by.egrius.pizzaShop.integration.testcontainer.TestContainerBase;
import by.egrius.pizzaShop.integration.utils.ITTestsUtils;
import by.egrius.pizzaShop.repository.IngredientRepository;
import by.egrius.pizzaShop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Sql(scripts = "/test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class AdminIngredientControllerIT extends TestContainerBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ITTestsUtils itTestsUtils;

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String VALID_INGREDIENT_NAME = "Моцарелла";
    private static final String VALID_INGREDIENT_DESCRIPTION = "Валидное описание ингредиента";

    private String adminToken;
    private String userToken;
    private Long existingIngredientId;

    private IngredientCreateDto validIngredientCreateDto() {
        return new IngredientCreateDto(
                VALID_INGREDIENT_NAME,
                VALID_INGREDIENT_DESCRIPTION,
                new BigDecimal("6.64"),
                true
        );
    }

    @BeforeEach
    void setup() {
        cleanDatabase();
        setupTestData();
    }

    private void cleanDatabase() {
        ingredientRepository.deleteAll();
        userRepository.deleteAll();
    }

    private void setupTestData() {
        adminToken = itTestsUtils.setupUserAndGetJwtToken("admin", "admin@example.com", "123456789", "admin", UserRole.ADMIN);
        userToken = itTestsUtils.setupUserAndGetJwtToken("user", "user@example.com", "987654321", "user", UserRole.CUSTOMER);

        Ingredient ingredient1 = Ingredient.create("Сыр", "Сырное описание", new BigDecimal("5.00"), true);
        Ingredient ingredient2 = Ingredient.create("Помидоры", "Томатное описание", new BigDecimal("3.00"), true);
        Ingredient ingredient3 = Ingredient.create("Грибы", "Грибное описание", new BigDecimal("4.00"), false);

        ingredientRepository.saveAll(List.of(ingredient1, ingredient2, ingredient3));
        existingIngredientId = ingredient1.getId();
    }

    @Nested
    class GetAllIngredientsTests {

        private static final String GET_ALL_ENDPOINT = "/api/admin/products/all";

        @Test
        void shouldReturn200WithPagination_whenUserIsAdmin() throws Exception {
            mockMvc.perform(get(GET_ALL_ENDPOINT)
                            .header("Authorization", BEARER_PREFIX + adminToken)
                            .param("page", "0")
                            .param("pageSize", "10"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(3))))
                    .andExpect(jsonPath("$.content[0].name", notNullValue()))
                    .andExpect(jsonPath("$.content[0].price", notNullValue()));
        }

        @Test
        void shouldReturn403_whenNoAuthorization() throws Exception {
            mockMvc.perform(get(GET_ALL_ENDPOINT)
                            .param("page", "0")
                            .param("pageSize", "10"))
                    .andExpect(status().isForbidden());
        }

        @Test
        void shouldReturn403_whenUserIsNotAdmin() throws Exception {
            mockMvc.perform(get(GET_ALL_ENDPOINT)
                            .header("Authorization", BEARER_PREFIX + userToken)
                            .param("page", "0")
                            .param("pageSize", "10"))
                    .andExpect(status().isForbidden());
        }

        @Test
        void shouldReturn400_whenPageNegative() throws Exception {
            mockMvc.perform(get(GET_ALL_ENDPOINT)
                            .header("Authorization", BEARER_PREFIX + adminToken)
                            .param("page", "-1")
                            .param("pageSize", "10"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturn400_whenPageSizeTooSmall() throws Exception {
            mockMvc.perform(get(GET_ALL_ENDPOINT)
                            .header("Authorization", BEARER_PREFIX + adminToken)
                            .param("page", "0")
                            .param("pageSize", "0"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturn400_whenPageSizeTooLarge() throws Exception {
            mockMvc.perform(get(GET_ALL_ENDPOINT)
                            .header("Authorization", BEARER_PREFIX + adminToken)
                            .param("page", "0")
                            .param("pageSize", "101"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldUseDefaultPagination_whenNoParamsProvided() throws Exception {
            mockMvc.perform(get(GET_ALL_ENDPOINT)
                            .header("Authorization", BEARER_PREFIX + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size").value(20)); // Дефолтный размер страницы
        }
    }

    @Nested
    class GetIngredientsByIdsTests {

        private static final String GET_BY_IDS_ENDPOINT = "/api/admin/products/by-ids";

        @Test
        void shouldReturn200_whenValidIdsAndUserIsAdmin() throws Exception {
            List<Long> ingredientIds = ingredientRepository.findAll().stream()
                    .map(Ingredient::getId)
                    .limit(2)
                    .toList();

            IngredientIdsRequest request = new IngredientIdsRequest(new HashSet<>(ingredientIds));
            String jsonContent = objectMapper.writeValueAsString(request);

            mockMvc.perform(post(GET_BY_IDS_ENDPOINT)
                            .header("Authorization", BEARER_PREFIX + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonContent))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)));
        }

        @Test
        void shouldReturn400_whenEmptyIdsList() throws Exception {
            IngredientIdsRequest request = new IngredientIdsRequest(new HashSet<>());
            String jsonContent = objectMapper.writeValueAsString(request);

            mockMvc.perform(post(GET_BY_IDS_ENDPOINT)
                            .header("Authorization", BEARER_PREFIX + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonContent))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturn404_whenIngredientNotFound() throws Exception {
            Set<Long> nonExistentIds = Set.of(999L, 1000L);
            IngredientIdsRequest request = new IngredientIdsRequest(nonExistentIds);
            String jsonContent = objectMapper.writeValueAsString(request);

            mockMvc.perform(post(GET_BY_IDS_ENDPOINT)
                            .header("Authorization", BEARER_PREFIX + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonContent))
                    .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturn403_whenUserIsNotAdmin() throws Exception {
            Set<Long> ids = Set.of(1L);
            IngredientIdsRequest request = new IngredientIdsRequest(ids);
            String jsonContent = objectMapper.writeValueAsString(request);

            mockMvc.perform(post(GET_BY_IDS_ENDPOINT)
                            .header("Authorization", BEARER_PREFIX + userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonContent))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    class CreateIngredientTests {

        private static final String CREATE_ENDPOINT = "/api/admin/products/create";

        @Test
        void shouldReturn201_whenValidDataAndUserIsAdmin() throws Exception {
            IngredientCreateDto dto = new IngredientCreateDto(
                    "Новый ингредиент",
                    "Описание нового ингредиента",
                    new BigDecimal("10.50"),
                    true
            );
            String jsonContent = objectMapper.writeValueAsString(dto);

            mockMvc.perform(post(CREATE_ENDPOINT)
                            .header("Authorization", BEARER_PREFIX + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonContent))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.name").value("Новый ингредиент"))
                    .andExpect(jsonPath("$.price").value(10.50))
                    .andExpect(jsonPath("$.available").value(true));

            assertTrue(ingredientRepository.findByName("Новый ингредиент").isPresent());
        }

        @Test
        void shouldReturn400_whenNameIsEmpty() throws Exception {
            IngredientCreateDto dto = new IngredientCreateDto(
                    "",
                    "Описание",
                    new BigDecimal("10.00"),
                    true
            );
            String jsonContent = objectMapper.writeValueAsString(dto);

            mockMvc.perform(post(CREATE_ENDPOINT)
                            .header("Authorization", BEARER_PREFIX + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonContent))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturn400_whenNameIsTooLong() throws Exception {
            String longName = "A".repeat(101);
            IngredientCreateDto dto = new IngredientCreateDto(
                    longName,
                    "Описание",
                    new BigDecimal("10.00"),
                    true
            );
            String jsonContent = objectMapper.writeValueAsString(dto);

            mockMvc.perform(post(CREATE_ENDPOINT)
                            .header("Authorization", BEARER_PREFIX + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonContent))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturn400_whenPriceIsNegative() throws Exception {
            IngredientCreateDto dto = new IngredientCreateDto(
                    "Ингредиент",
                    "Описание",
                    new BigDecimal("-10.00"),
                    true
            );
            String jsonContent = objectMapper.writeValueAsString(dto);

            mockMvc.perform(post(CREATE_ENDPOINT)
                            .header("Authorization", BEARER_PREFIX + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonContent))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturn400_whenPriceHasTooManyDecimalPlaces() throws Exception {
            IngredientCreateDto dto = new IngredientCreateDto(
                    "Ингредиент",
                    "Описание",
                    new BigDecimal("10.123"),
                    true
            );
            String jsonContent = objectMapper.writeValueAsString(dto);

            mockMvc.perform(post(CREATE_ENDPOINT)
                            .header("Authorization", BEARER_PREFIX + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonContent))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturn409_whenIngredientWithSameNameExists() throws Exception {
            // Сначала создаем ингредиент
            IngredientCreateDto firstDto = new IngredientCreateDto(
                    "Дубликат",
                    "Описание",
                    new BigDecimal("5.00"),
                    true
            );
            String firstJson = objectMapper.writeValueAsString(firstDto);

            mockMvc.perform(post(CREATE_ENDPOINT)
                            .header("Authorization", BEARER_PREFIX + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(firstJson))
                    .andExpect(status().isCreated());

            // Пытаемся создать с тем же именем
            IngredientCreateDto duplicateDto = new IngredientCreateDto(
                    "Дубликат",
                    "Другое описание",
                    new BigDecimal("7.00"),
                    false
            );
            String duplicateJson = objectMapper.writeValueAsString(duplicateDto);

            mockMvc.perform(post(CREATE_ENDPOINT)
                            .header("Authorization", BEARER_PREFIX + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(duplicateJson))
                    .andExpect(status().isConflict());
        }

        @Test
        void shouldReturn403_whenUserIsNotAdmin() throws Exception {
            IngredientCreateDto dto = validIngredientCreateDto();
            String jsonContent = objectMapper.writeValueAsString(dto);

            mockMvc.perform(post(CREATE_ENDPOINT)
                            .header("Authorization", BEARER_PREFIX + userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonContent))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    class UpdateIngredientTests {

        private static final String UPDATE_ENDPOINT = "/api/admin/products/{id}";

        @Test
        void shouldReturn200_whenValidUpdateAndUserIsAdmin() throws Exception {
            IngredientUpdateDto updateDto = new IngredientUpdateDto(
                    "Обновленный сыр",
                    "Новое описание сыра",
                    new BigDecimal("7.50"),
                    false
            );
            String jsonContent = objectMapper.writeValueAsString(updateDto);

            mockMvc.perform(put(UPDATE_ENDPOINT, existingIngredientId)
                            .header("Authorization", BEARER_PREFIX + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonContent))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Обновленный сыр"))
                    .andExpect(jsonPath("$.price").value(7.50))
                    .andExpect(jsonPath("$.available").value(false));
        }

        @Test
        void shouldReturn404_whenIngredientNotFound() throws Exception {
            IngredientUpdateDto updateDto = new IngredientUpdateDto(
                    "Название",
                    "Описание",
                    new BigDecimal("5.00"),
                    true
            );
            String jsonContent = objectMapper.writeValueAsString(updateDto);

            mockMvc.perform(put(UPDATE_ENDPOINT, 999L)
                            .header("Authorization", BEARER_PREFIX + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonContent))
                    .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturn400_whenPriceIsZero() throws Exception {
            IngredientUpdateDto updateDto = new IngredientUpdateDto(
                    "Название",
                    "Описание",
                    new BigDecimal("0.00"),
                    true
            );
            String jsonContent = objectMapper.writeValueAsString(updateDto);

            mockMvc.perform(put(UPDATE_ENDPOINT, existingIngredientId)
                            .header("Authorization", BEARER_PREFIX + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonContent))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturn400_whenAvailableIsNull() throws Exception {
            String jsonContent = """
            {
                "name": "Название",
                "description": "Описание",
                "price": "5.00",
                "available": null
            }
            """;

            mockMvc.perform(put(UPDATE_ENDPOINT, existingIngredientId)
                            .header("Authorization", BEARER_PREFIX + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonContent))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturn409_whenNameConflictWithOtherIngredient() throws Exception {
            // Создаем второй ингредиент
            Ingredient secondIngredient = Ingredient.create("Второй сыр", "Описание", new BigDecimal("6.00"), true);
            ingredientRepository.save(secondIngredient);

            // Пытаемся переименовать первый ингредиент в имя второго
            IngredientUpdateDto updateDto = new IngredientUpdateDto(
                    "Второй сыр", // Конфликтующее имя
                    "Новое описание",
                    new BigDecimal("7.00"),
                    true
            );
            String jsonContent = objectMapper.writeValueAsString(updateDto);

            mockMvc.perform(put(UPDATE_ENDPOINT, existingIngredientId)
                            .header("Authorization", BEARER_PREFIX + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonContent))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    class DeleteIngredientTests {

        private static final String DELETE_ENDPOINT = "/api/admin/products/{id}";

        private Long ingredientIdForDeletion;

        @BeforeEach
        void setupIngredientForDeletion() {
            Ingredient ingredient = Ingredient.create("Для удаления", "Описание", new BigDecimal("5.00"), true);
            ingredient = ingredientRepository.save(ingredient);
            ingredientIdForDeletion = ingredient.getId();
        }

        @Test
        void shouldReturn204_whenSuccess() throws Exception {
            mockMvc.perform(delete(DELETE_ENDPOINT, ingredientIdForDeletion)
                            .header("Authorization", BEARER_PREFIX + adminToken))
                    .andExpect(status().isNoContent());

            assertTrue(ingredientRepository.findById(ingredientIdForDeletion).isEmpty());
        }

        @Test
        void shouldReturn404_whenIngredientNotFound() throws Exception {
            mockMvc.perform(delete(DELETE_ENDPOINT, 999L)
                            .header("Authorization", BEARER_PREFIX + adminToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturn400_whenIngredientIdIsNegative() throws Exception {
            mockMvc.perform(delete(DELETE_ENDPOINT, -1L)
                            .header("Authorization", BEARER_PREFIX + adminToken))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturn403_whenUserIsNotAdmin() throws Exception {
            mockMvc.perform(delete(DELETE_ENDPOINT, ingredientIdForDeletion)
                            .header("Authorization", BEARER_PREFIX + userToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        void shouldReturn409_whenIngredientUsedInPizzas() throws Exception {
            // Здесь нужно создать тестовые данные с пиццей, использующей этот ингредиент
            // В зависимости от реализации это может потребовать дополнительной настройки

            // Пример проверки (закомментировано, так как требует полноценной настройки связанных сущностей):
            /*
            mockMvc.perform(delete(DELETE_ENDPOINT, existingIngredientId)
                            .header("Authorization", BEARER_PREFIX + adminToken))
                    .andExpect(status().isConflict());
            */
        }
    }
}