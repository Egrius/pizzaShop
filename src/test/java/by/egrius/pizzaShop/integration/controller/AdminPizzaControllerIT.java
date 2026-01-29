package by.egrius.pizzaShop.integration.controller;

import by.egrius.pizzaShop.dto.pizza.PizzaCreateDto;
import by.egrius.pizzaShop.dto.pizza.PizzaUpdateDto;
import by.egrius.pizzaShop.entity.*;
import by.egrius.pizzaShop.repository.*;
import by.egrius.pizzaShop.security.UserDetailsServiceImpl;
import by.egrius.pizzaShop.security.jwt.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Sql(scripts = "/test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class AdminPizzaControllerIT {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PizzaRepository pizzaRepository;

    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    private SizeTemplateRepository sizeTemplateRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtils jwtUtils;

    private static final String BEARER_PREFIX = "Bearer ";

    private static final String VALID_NAME = "Маргарита";
    private static final String VALID_DESCRIPTION = "Классическая пицца";
    private static final String VALID_IMAGE_URL = "https://images/margherita.jpg";
    private static final String VALID_CATEGORY = "Классические";

    private String adminToken;
    private String userToken;
    private PizzaCreateDto validPizzaDto;

    @BeforeEach
    void setup() {
        cleanDatabase();
        setupTestData();
        validPizzaDto = createValidPizzaDto();
    }

    private void cleanDatabase() {
        userRepository.deleteAll();
    }

    private void setupTestData() {
        setupIngredients();
        setupSizeTemplates();
        setupUsersWithTokens();
    }

    private void setupIngredients() {
        ingredientRepository.saveAll(List.of(
                Ingredient.create("Моцарелла", "...", new BigDecimal("6.64"), true),
                Ingredient.create("Цыплёнок", "...", new BigDecimal("10.0"), true),
                Ingredient.create("Бекон", "...", new BigDecimal("1.60"), true)
        ));
    }

    private void setupSizeTemplates() {
        sizeTemplateRepository.saveAll(List.of(
                SizeTemplate.create(PizzaSizeEnum.SMALL, "25см", 25, 450, new BigDecimal("0.8")),
                SizeTemplate.create(PizzaSizeEnum.MEDIUM, "30см", 30, 650, BigDecimal.ONE)
        ));
    }

    private void setupUsersWithTokens() {
        userRepository.saveAll(List.of(
                User.createUserCustom("admin", "admin@example.com", "123456789", "admin", UserRole.ADMIN),
                User.createUserCustom("user", "user@example.com", "987654321", "user", UserRole.CUSTOMER)
        ));

        adminToken = generateToken("admin@example.com");
        userToken = generateToken("user@example.com");
    }

    private String generateToken(String email) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        return jwtUtils.generateJwtToken(authentication);
    }

    private PizzaCreateDto createValidPizzaDto() {
        return new PizzaCreateDto(
                VALID_NAME,
                VALID_DESCRIPTION,
                VALID_IMAGE_URL,
                VALID_CATEGORY,
                true,
                15,
                Map.of(1L, 200, 2L, 250, 3L, 40),
                Set.of(1L, 2L)
        );
    }

    @Nested
    class CreatePizzaTests {

        private static final String ADMIN_CREATE_ENDPOINT = "/api/admin/pizzas/create";

        @Test
        void shouldBe201_whenCorrectDataAndUserIsAdmin() throws Exception {
            PizzaCreateDto dto = createValidPizzaDto();
            String jsonContent = objectMapper.writeValueAsString(dto);

            mockMvc.perform(post(ADMIN_CREATE_ENDPOINT)
                            .header("Authorization", BEARER_PREFIX + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonContent))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.name").value("Маргарита"))
                    .andExpect(jsonPath("$.description").value("Классическая пицца"));
        }

        @Test
        void shouldBe403_whenNoAuthorization() throws Exception {
            PizzaCreateDto dto = createValidPizzaDto();
            String jsonContent = objectMapper.writeValueAsString(dto);

            mockMvc.perform(post(ADMIN_CREATE_ENDPOINT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonContent))
                    .andExpect(status().isForbidden());
        }

        @Test
        void shouldBe403_whenUserIsNotAdmin() throws Exception {
            PizzaCreateDto dto = createValidPizzaDto();
            String jsonContent = objectMapper.writeValueAsString(dto);

            mockMvc.perform(post(ADMIN_CREATE_ENDPOINT)
                            .header("Authorization", BEARER_PREFIX + userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonContent))
                    .andExpect(status().isForbidden());
        }

        @Test
        void shouldBe400_whenNameIsEmpty() throws Exception {
            PizzaCreateDto dto = new PizzaCreateDto(
                    "",
                    "Классическая пицца",
                    "https://images/margherita.jpg",
                    "Классические",
                    true,
                    15,
                    Map.of(1L, 200),
                    Set.of(1L)
            );
            String jsonContent = objectMapper.writeValueAsString(dto);

            mockMvc.perform(post(ADMIN_CREATE_ENDPOINT)
                            .header("Authorization", BEARER_PREFIX + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonContent))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.violations[?(@.field == 'name')]").exists());
        }

        @Test
        void shouldBe400_whenNameIsTooShort() throws Exception {
            PizzaCreateDto dto = new PizzaCreateDto(
                    "A",
                    "Классическая пицца",
                    "https://images/margherita.jpg",
                    "Классические",
                    true,
                    15,
                    Map.of(1L, 200),
                    Set.of(1L)
            );
            String jsonContent = objectMapper.writeValueAsString(dto);

            mockMvc.perform(post(ADMIN_CREATE_ENDPOINT)
                            .header("Authorization", BEARER_PREFIX + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonContent))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldBe400_whenCookingTimeNegative() throws Exception {
            PizzaCreateDto dto = new PizzaCreateDto(
                    "Маргарита",
                    "Классическая пицца",
                    "https://images/margherita.jpg",
                    "Классические",
                    true,
                    -5,
                    Map.of(1L, 200),
                    Set.of(1L)
            );
            String jsonContent = objectMapper.writeValueAsString(dto);

            mockMvc.perform(post(ADMIN_CREATE_ENDPOINT)
                            .header("Authorization", BEARER_PREFIX + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonContent))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.violations[?(@.field == 'cookingTimeMinutes')]").exists());
        }

        @Test
        void shouldBe400_whenImageUrlIsInvalid() throws Exception {
            PizzaCreateDto dto = new PizzaCreateDto(
                    "Маргарита",
                    "Классическая пицца",
                    "not-a-valid-url",
                    "Классические",
                    true,
                    15,
                    Map.of(1L, 200),
                    Set.of(1L)
            );
            String jsonContent = objectMapper.writeValueAsString(dto);

            mockMvc.perform(post(ADMIN_CREATE_ENDPOINT)
                            .header("Authorization", BEARER_PREFIX + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonContent))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.violations[?(@.field == 'imageUrl')]").exists());
        }

        @Test
        void shouldBe400_whenRequiredFieldsMissing() throws Exception {
            String invalidJson = """
            {
                "name": "Маргарита"
                // Остальные обязательные поля отсутствуют
            }
            """;

            mockMvc.perform(post(ADMIN_CREATE_ENDPOINT)
                            .header("Authorization", BEARER_PREFIX + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldBe400_whenIngredientsEmpty() throws Exception {
            PizzaCreateDto dto = new PizzaCreateDto(
                    "Маргарита",
                    "Классическая пицца",
                    "https://images/margherita.jpg",
                    "Классические",
                    true,
                    15,
                    Map.of(), // Пустой список ингредиентов
                    Set.of(1L)
            );
            String jsonContent = objectMapper.writeValueAsString(dto);

            mockMvc.perform(post(ADMIN_CREATE_ENDPOINT)
                            .header("Authorization", BEARER_PREFIX + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonContent))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldBe400_whenSizeTemplatesEmpty() throws Exception {
            PizzaCreateDto dto = new PizzaCreateDto(
                    "Маргарита",
                    "Классическая пицца",
                    "https://images/margherita.jpg",
                    "Классические",
                    true,
                    15,
                    Map.of(1L, 200),
                    Set.of() // Пустой список размеров
            );
            String jsonContent = objectMapper.writeValueAsString(dto);

            mockMvc.perform(post(ADMIN_CREATE_ENDPOINT)
                            .header("Authorization", BEARER_PREFIX + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonContent))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldBe409_whenPizzaWithSameNameExists() throws Exception {
            PizzaCreateDto firstPizza = createValidPizzaDto();
            String firstJson = objectMapper.writeValueAsString(firstPizza);

            mockMvc.perform(post(ADMIN_CREATE_ENDPOINT)
                            .header("Authorization", BEARER_PREFIX + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(firstJson))
                    .andExpect(status().isCreated());

            PizzaCreateDto duplicatePizza = new PizzaCreateDto(
                    "Маргарита",
                    "Другое описание",
                    "https://images/another.jpg",
                    "Другие",
                    true,
                    20,
                    Map.of(1L, 150),
                    Set.of(1L)
            );
            String duplicateJson = objectMapper.writeValueAsString(duplicatePizza);

            mockMvc.perform(post(ADMIN_CREATE_ENDPOINT)
                            .header("Authorization", BEARER_PREFIX + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(duplicateJson))
                    .andExpect(status().isConflict());
        }

        @Test
        void shouldBe404_whenIngredientNotFound() throws Exception {
            PizzaCreateDto dto = new PizzaCreateDto(
                    "Маргарита",
                    "Классическая пицца",
                    "https://images/margherita.jpg",
                    "Классические",
                    true,
                    15,
                    Map.of(999L, 200),
                    Set.of(1L)
            );
            String jsonContent = objectMapper.writeValueAsString(dto);

            mockMvc.perform(post(ADMIN_CREATE_ENDPOINT)
                            .header("Authorization", BEARER_PREFIX + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonContent))
                    .andExpect(status().isNotFound());
        }

        @Test
        void shouldBe404_whenSizeTemplateNotFound() throws Exception {
            PizzaCreateDto dto = new PizzaCreateDto(
                    "Маргарита",
                    "Классическая пицца",
                    "https://images/margherita.jpg",
                    "Классические",
                    true,
                    15,
                    Map.of(1L, 200),
                    Set.of(999L)
            );
            String jsonContent = objectMapper.writeValueAsString(dto);

            mockMvc.perform(post(ADMIN_CREATE_ENDPOINT)
                            .header("Authorization", BEARER_PREFIX + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonContent))
                    .andExpect(status().isNotFound());
        }

        @Test
        void shouldBe400_whenIngredientWeightNegative() throws Exception {
            PizzaCreateDto dto = new PizzaCreateDto(
                    "Маргарита",
                    "Классическая пицца",
                    "https://images/margherita.jpg",
                    "Классические",
                    true,
                    15,
                    Map.of(1L, -50),
                    Set.of(1L)
            );
            String jsonContent = objectMapper.writeValueAsString(dto);

            mockMvc.perform(post(ADMIN_CREATE_ENDPOINT)
                            .header("Authorization", BEARER_PREFIX + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonContent))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldBe415_whenWrongContentType() throws Exception {
            PizzaCreateDto dto = createValidPizzaDto();
            String jsonContent = objectMapper.writeValueAsString(dto);

            mockMvc.perform(post(ADMIN_CREATE_ENDPOINT)
                            .header("Authorization", BEARER_PREFIX + adminToken)
                            .contentType(MediaType.TEXT_PLAIN)
                            .content(jsonContent))
                    .andExpect(status().isUnsupportedMediaType());
        }

        @Test
        void shouldBe400_whenInvalidJson() throws Exception {
            String invalidJson = "{ invalid json }";

            mockMvc.perform(post(ADMIN_CREATE_ENDPOINT)
                            .header("Authorization", BEARER_PREFIX + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldBe400_whenDescriptionTooLong() throws Exception {
            String longDescription = "A".repeat(1001);

            PizzaCreateDto dto = new PizzaCreateDto(
                    "Маргарита",
                    longDescription,
                    "https://images/margherita.jpg",
                    "Классические",
                    true,
                    15,
                    Map.of(1L, 200),
                    Set.of(1L)
            );
            String jsonContent = objectMapper.writeValueAsString(dto);

            mockMvc.perform(post(ADMIN_CREATE_ENDPOINT)
                            .header("Authorization", BEARER_PREFIX + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonContent))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class UpdatePizzaTests {

        private Long existingPizzaId;

        @Autowired
        private PizzaIngredientRepository pizzaIngredientRepository;

        @Autowired
        private PizzaSizeRepository pizzaSizeRepository;

        @BeforeEach
        void setupExistingPizza() {
            Pizza pizza = Pizza.create(
                    "Супер Пепперони",
                    "Острая пицца",
                    "https://images/pepperoni.jpg",
                    "Мясные",
                    true,
                    20
            );
            pizza = pizzaRepository.save(pizza);

            Ingredient ingredient = ingredientRepository.findById(1L).orElseThrow();
            SizeTemplate size = sizeTemplateRepository.findById(1L).orElseThrow();

            PizzaIngredient pizzaIngredient = new PizzaIngredient();
            pizzaIngredient.setPizza(pizza);
            pizzaIngredient.setIngredient(ingredient);
            pizzaIngredient.setWeightGrams(200);

            pizzaIngredientRepository.save(pizzaIngredient);

            PizzaSize pizzaSize = PizzaSize.create(pizza, size, BigDecimal.valueOf(25.0), true);
            pizzaSizeRepository.save(pizzaSize);

            pizza.getPizzaIngredients().add(pizzaIngredient);
            pizza.getPizzaSizes().add(pizzaSize);

            existingPizzaId = pizza.getId();
        }

        private PizzaUpdateDto createValidUpdateDto() {
            return new PizzaUpdateDto(
                    "Обновленная Пепперони",
                    "Новое описание",
                    "https://images/new-pepperoni.jpg",
                    "Обновленные",
                    false,
                    25
            );
        }

        @Test
        void updatePizza_shouldReturn200_whenSuccess() throws Exception {
            PizzaUpdateDto updateDto = createValidUpdateDto();
            String jsonContent = objectMapper.writeValueAsString(updateDto);

            mockMvc.perform(put("/api/admin/pizzas/{id}", existingPizzaId)
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonContent))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Обновленная Пепперони"))
                    .andExpect(jsonPath("$.description").value("Новое описание"))
                    .andExpect(jsonPath("$.isAvailable").value(false))
                    .andExpect(jsonPath("$.cookingTimeMinutes").value(25));
        }


        @Test
        void updatePizza_shouldReturn404_whenPizzaNotFound() throws Exception {
            Long nonExistentId = 999L;
            PizzaUpdateDto updateDto = createValidUpdateDto();
            String jsonContent = objectMapper.writeValueAsString(updateDto);

            mockMvc.perform(put("/api/admin/pizzas/{id}", nonExistentId)
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonContent))
                    .andExpect(status().isNotFound());
        }

        @Test
        void updatePizza_shouldReturn403_whenNoAuthorization() throws Exception {
            PizzaUpdateDto updateDto = createValidUpdateDto();
            String jsonContent = objectMapper.writeValueAsString(updateDto);

            mockMvc.perform(put("/api/admin/pizzas/{id}", existingPizzaId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonContent))
                    .andExpect(status().isForbidden());
        }

        @Test
        void updatePizza_shouldReturn403_whenUserIsNotAdmin() throws Exception {
            PizzaUpdateDto updateDto = createValidUpdateDto();
            String jsonContent = objectMapper.writeValueAsString(updateDto);

            mockMvc.perform(put("/api/admin/pizzas/{id}", existingPizzaId)
                            .header("Authorization", "Bearer " + userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonContent))
                    .andExpect(status().isForbidden());
        }

        @Test
        void updatePizza_shouldReturn200_whenNameIsNullAndOtherFieldsAreCorrect() throws Exception {
            PizzaUpdateDto invalidDto = new PizzaUpdateDto(
                    null,
                    "Описание",
                    "https://images/test.jpg",
                    "Категория",
                    true,
                    15
            );
            String jsonContent = objectMapper.writeValueAsString(invalidDto);

            mockMvc.perform(put("/api/admin/pizzas/{id}", existingPizzaId)
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonContent))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Супер Пепперони"));
        }

        @Test
        void updatePizza_shouldReturn400_whenCookingTimeNegative() throws Exception {
            PizzaUpdateDto invalidDto = new PizzaUpdateDto(
                    "Название",
                    "Описание",
                    "https://images/test.jpg",
                    "Категория",
                    true,
                    -5
            );
            String jsonContent = objectMapper.writeValueAsString(invalidDto);

            mockMvc.perform(put("/api/admin/pizzas/{id}", existingPizzaId)
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonContent))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void updatePizza_shouldReturn400_whenImageUrlInvalid() throws Exception {
            PizzaUpdateDto invalidDto = new PizzaUpdateDto(
                    "Название",
                    "Описание",
                    "not-a-valid-url",
                    "Категория",
                    true,
                    15
            );
            String jsonContent = objectMapper.writeValueAsString(invalidDto);

            mockMvc.perform(put("/api/admin/pizzas/{id}", existingPizzaId)
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonContent))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void updatePizza_shouldReturn409_whenNameConflict() throws Exception {
            Pizza secondPizza = Pizza.create(
                    "Другая пицца",
                    "Описание",
                    "https://images/other.jpg",
                    "Категория",
                    true,
                    15
            );
            Long anotherPizzaId = secondPizza.getId();
            pizzaRepository.save(secondPizza);

            PizzaUpdateDto conflictDto = new PizzaUpdateDto(
                    "Другая пицца",
                    "Новое описание",
                    "https://images/new.jpg",
                    "Новая категория",
                    false,
                    20
            );
            String jsonContent = objectMapper.writeValueAsString(conflictDto);

            mockMvc.perform(put("/api/admin/pizzas/{id}", existingPizzaId)
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonContent))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value(containsString("уже существует")));
        }

        @Test
        void updatePizza_shouldReturn400_whenDescriptionTooLong() throws Exception {
            String longDescription = "A".repeat(1001);

            PizzaUpdateDto invalidDto = new PizzaUpdateDto(
                    "Название",
                    longDescription,
                    "https://images/test.jpg",
                    "Категория",
                    true,
                    15
            );
            String jsonContent = objectMapper.writeValueAsString(invalidDto);

            mockMvc.perform(put("/api/admin/pizzas/{id}", existingPizzaId)
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonContent))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void updatePizza_shouldReturn400_whenAllFieldsAreNull() throws Exception {
            PizzaUpdateDto invalidDto = new PizzaUpdateDto(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
            String jsonContent = objectMapper.writeValueAsString(invalidDto);

            mockMvc.perform(put("/api/admin/pizzas/{id}", existingPizzaId)
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonContent))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class DeletePizzaTests {
        private Long pizzaIdToDelete;

        @BeforeEach
        void setupPizzaForDeletion() {
            Pizza pizza = Pizza.create("Для удаления", "...", "...", "...", true, 15);
            pizza = pizzaRepository.save(pizza);
            pizzaIdToDelete = pizza.getId();
        }

        @Test
        void deletePizza_shouldReturn204_whenSuccess() throws Exception {
            mockMvc.perform(delete("/api/admin/pizzas/{id}", pizzaIdToDelete)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isNoContent());

            assertThat(pizzaRepository.findById(pizzaIdToDelete)).isEmpty();
        }

        @Test
        void deletePizza_shouldReturn404_whenPizzaNotFound() throws Exception {
            mockMvc.perform(delete("/api/admin/pizzas/999")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        void deletePizza_shouldReturn403_whenUserIsNotAdmin() throws Exception {
            mockMvc.perform(delete("/api/admin/pizzas/{id}", pizzaIdToDelete)
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isForbidden());
        }
    }
}