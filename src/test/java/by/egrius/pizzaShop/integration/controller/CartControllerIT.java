package by.egrius.pizzaShop.integration.controller;

import by.egrius.pizzaShop.controller.customer.CartController;
import by.egrius.pizzaShop.dto.cart_item.CartItemCreateDto;
import by.egrius.pizzaShop.entity.*;
import by.egrius.pizzaShop.event.publisher.OrderEventPublisher;
import by.egrius.pizzaShop.integration.testcontainer.TestContainerBase;
import by.egrius.pizzaShop.payment_imitation.PaymentDetails;
import by.egrius.pizzaShop.payment_imitation.PaymentService;
import by.egrius.pizzaShop.repository.*;
import by.egrius.pizzaShop.security.UserDetailsImpl;
import by.egrius.pizzaShop.security.UserDetailsServiceImpl;
import by.egrius.pizzaShop.security.jwt.JwtUtils;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "/test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class CartControllerIT extends TestContainerBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CartController cartController;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @MockitoSpyBean
    private OrderEventPublisher orderEventPublisher;

    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    private SizeTemplateRepository sizeTemplateRepository;

    @Autowired
    private PizzaSizeRepository pizzaSizeRepository;

    @Autowired
    private PizzaIngredientRepository pizzaIngredientRepository;

    @Autowired
    private PizzaRepository pizzaRepository;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtils jwtUtils;

    private static final String BEARER_PREFIX = "Bearer ";

    private final Address MOCK_ADDRESS =  new Address("ул. Тестовая", "1", "1", "1", "Минск");
    private final PaymentDetails MOCK_PAYMENT_DETAILS = new PaymentDetails("4111111111111111", "TEST USER", "12/30", "123");

    private User customer;
    private String customerToken;
    private Pizza testPizza;
    private PizzaSize testPizzaSizeMedium;
    private PizzaSize testPizzaSizeLarge;
    private CartItem existingCartItem;

    @BeforeEach
    @Transactional
    void setup() {

        customer = userRepository.findByEmail("customer@test.com")
                .orElseGet(() -> {
                    User newUser = User.createUserCustom(
                            "Test Customer",
                            "customer@test.com",
                            "+375291112233",
                            "password123",
                            UserRole.CUSTOMER
                    );
                    return userRepository.save(newUser);
                });


        UserDetailsImpl userDetails = new UserDetailsImpl(customer);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );

        // Генерируем токен
        customerToken = "Bearer " + jwtUtils.generateJwtToken(authentication);

        // 3. Создаем полноценную пиццу со всеми зависимостями
        if (pizzaRepository.findByName("Тестовая Маргарита").isEmpty()) {
            createTestPizzaWithAllDependencies();
        }

        // 4. Находим созданную пиццу
        testPizza = pizzaRepository.findByName("Тестовая Маргарита")
                .orElseThrow();

        testPizzaSizeMedium = pizzaSizeRepository.findAll().stream()
                .filter(ps -> ps.getPizza().getId().equals(testPizza.getId()))
                .filter(ps -> ps.getSizeTemplate().getSizeName() == PizzaSizeEnum.MEDIUM)
                .findFirst()
                .orElseThrow();

        testPizzaSizeLarge = pizzaSizeRepository.findAll().stream()
                .filter(ps -> ps.getPizza().getId().equals(testPizza.getId()))
                .filter(ps -> ps.getSizeTemplate().getSizeName() == PizzaSizeEnum.LARGE)
                .findFirst()
                .orElseThrow();

        cartItemRepository.deleteByUser(customer);
    }

    private void createTestPizzaWithAllDependencies() {
        // Создаем шаблон размера
        SizeTemplate mediumTemplate = SizeTemplate.create(
                PizzaSizeEnum.MEDIUM,
                "30 см",
                30,
                650,
                new BigDecimal("1.2")
        );

        SizeTemplate largeTemplate = SizeTemplate.create(
                PizzaSizeEnum.LARGE,
                "35 см",
                35,
                850,
                new BigDecimal("1.5")
        );

        sizeTemplateRepository.save(mediumTemplate);
        sizeTemplateRepository.save(largeTemplate);

        // Создаем ингредиенты
        Ingredient cheese = Ingredient.create(
                "Сыр Моцарелла",
                "Итальянский сыр",
                new BigDecimal("5.00"),
                true
        );
        ingredientRepository.save(cheese);

        Ingredient tomatoSauce = Ingredient.create(
                "Томатный соус",
                "Натуральный томатный соус",
                new BigDecimal("2.50"),
                true
        );
        ingredientRepository.save(tomatoSauce);

        // Создаем пиццу
        Pizza pizza = Pizza.create(
                "Тестовая Маргарита",
                "Классическая пицца Маргарита для тестов",
                "/images/margarita.jpg",
                "Классические",
                true,
                15
        );
        Pizza savedPizza = pizzaRepository.save(pizza);

        // Создаем связи пицца-ингредиенты
        PizzaIngredient pizzaCheese = new PizzaIngredient();
        pizzaCheese.setPizza(savedPizza);
        pizzaCheese.setIngredient(cheese);
        pizzaCheese.setWeightGrams(200);
        pizzaIngredientRepository.save(pizzaCheese);

        PizzaIngredient pizzaSauce = new PizzaIngredient();
        pizzaSauce.setPizza(savedPizza);
        pizzaSauce.setIngredient(tomatoSauce);
        pizzaSauce.setWeightGrams(100);
        pizzaIngredientRepository.save(pizzaSauce);

        savedPizza.setPizzaIngredients(Set.of(pizzaCheese, pizzaSauce));

        // Создаем размеры пиццы
        PizzaSize mediumSize = PizzaSize.create(
                savedPizza,
                mediumTemplate,
                new BigDecimal("25.00"), // Базовая цена для Medium
                true
        );
        pizzaSizeRepository.save(mediumSize);

        PizzaSize largeSize = PizzaSize.create(
                savedPizza,
                largeTemplate,
                new BigDecimal("35.00"),
                true
        );
        pizzaSizeRepository.save(largeSize);
    }

    private void addItemToCart(User user, Pizza pizza, PizzaSize size, int quantity) {
        CartItem cartItem = new CartItem();
        cartItem.setPizza(pizza);
        cartItem.setUser(user);
        cartItem.setQuantity(quantity);
        cartItem.setPizzaSize(size);
        cartItem.setAddedAt(LocalDateTime.now());
        cartItemRepository.save(cartItem);
    }

    private CartItem createCartItem(User user, Pizza pizza, PizzaSize size, int quantity) {
        CartItem cartItem = new CartItem();
        cartItem.setPizza(pizza);
        cartItem.setUser(user);
        cartItem.setQuantity(quantity);
        cartItem.setPizzaSize(size);
        cartItem.setAddedAt(LocalDateTime.now());
        return cartItemRepository.save(cartItem);
    }

    @Nested
    class GetCartTests {

        @Test
        void shouldReturnCustomerCart() throws Exception {
            addItemToCart(customer, testPizza, testPizzaSizeMedium, 2);

            mockMvc.perform(get("/api/cart")
                            .header("Authorization", customerToken))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.cartItemReadDtos").isArray())
                    .andExpect(jsonPath("$.cartItemReadDtos.length()").value(1))
                    .andExpect(jsonPath("$.cartItemReadDtos[0].pizzaReadDto.name").value("Тестовая Маргарита"));

        }

        @Test
        void shouldReturnEmptyCartWhenNoItems() throws Exception {
            mockMvc.perform(get("/api/cart")
                            .header("Authorization", customerToken))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.cartItemReadDtos").isArray())
                    .andExpect(jsonPath("$.cartItemReadDtos.length()").value(0))
                    .andExpect(jsonPath("$.totalPrice").value(0));
        }

        @Test
        void shouldReturnUnauthorizedWhenNoToken() throws Exception {
            mockMvc.perform(get("/api/cart"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    class AddToCartTests {

        @Test
        void shouldAddToCartCorrect() throws Exception {
            CartItemCreateDto createDto = new CartItemCreateDto(
                    testPizza.getId(),
                    testPizzaSizeLarge.getId(),
                    1
            );

            String createDtoJson = objectMapper.writeValueAsString(createDto);

            mockMvc.perform(post("/api/cart")
                            .header("Authorization", customerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createDtoJson))
                    .andExpect(status().isCreated());

            List<CartItem> cartItems = cartItemRepository.findByUser(customer);
            assertNotNull(cartItems);
            assertEquals(1, cartItems.size());
            assertEquals(testPizzaSizeLarge.getId(), cartItems.get(0).getPizzaSize().getId());
            assertEquals(1, cartItems.get(0).getQuantity());
        }

        @Test
        void shouldIncreaseQuantityWhenAddingSamePizzaSize() throws Exception {
            // Сначала добавляем один элемент
            addItemToCart(customer, testPizza, testPizzaSizeMedium, 1);

            CartItemCreateDto createDto = new CartItemCreateDto(
                    testPizza.getId(),
                    testPizzaSizeMedium.getId(),
                    2
            );

            String createDtoJson = objectMapper.writeValueAsString(createDto);

            mockMvc.perform(post("/api/cart")
                            .header("Authorization", customerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createDtoJson))
                    .andExpect(status().isCreated());

            List<CartItem> cartItems = cartItemRepository.findByUser(customer);
            assertEquals(1, cartItems.size());
            assertEquals(3, cartItems.get(0).getQuantity()); // 1 + 2 = 3
        }

        @Test
        void shouldAddDifferentPizzaSizeAsNewItem() throws Exception {
            // Сначала добавляем среднюю пиццу
            addItemToCart(customer, testPizza, testPizzaSizeMedium, 1);

            // Добавляем большую пиццу
            CartItemCreateDto createDto = new CartItemCreateDto(
                    testPizza.getId(),
                    testPizzaSizeLarge.getId(),
                    1
            );

            String createDtoJson = objectMapper.writeValueAsString(createDto);

            mockMvc.perform(post("/api/cart")
                            .header("Authorization", customerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createDtoJson))
                    .andExpect(status().isCreated());

            List<CartItem> cartItems = cartItemRepository.findByUser(customer);
            assertEquals(2, cartItems.size());
        }

        @Test
        void shouldReturnBadRequestWhenPizzaNotFound() throws Exception {
            CartItemCreateDto createDto = new CartItemCreateDto(
                    999999L, // Несуществующий ID
                    testPizzaSizeMedium.getId(),
                    1
            );

            String createDtoJson = objectMapper.writeValueAsString(createDto);

            mockMvc.perform(post("/api/cart")
                            .header("Authorization", customerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createDtoJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturnBadRequestWhenPizzaSizeNotFound() throws Exception {
            CartItemCreateDto createDto = new CartItemCreateDto(
                    testPizza.getId(),
                    999999L, // Несуществующий ID
                    1
            );

            String createDtoJson = objectMapper.writeValueAsString(createDto);

            mockMvc.perform(post("/api/cart")
                            .header("Authorization", customerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createDtoJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturnBadRequestWhenQuantityIsZero() throws Exception {
            CartItemCreateDto createDto = new CartItemCreateDto(
                    testPizza.getId(),
                    testPizzaSizeMedium.getId(),
                    0
            );

            String createDtoJson = objectMapper.writeValueAsString(createDto);

            mockMvc.perform(post("/api/cart")
                            .header("Authorization", customerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createDtoJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturnBadRequestWhenQuantityIsNegative() throws Exception {
            CartItemCreateDto createDto = new CartItemCreateDto(
                    testPizza.getId(),
                    testPizzaSizeMedium.getId(),
                    -1
            );

            String createDtoJson = objectMapper.writeValueAsString(createDto);

            mockMvc.perform(post("/api/cart")
                            .header("Authorization", customerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createDtoJson))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturnUnauthorizedWhenNoToken() throws Exception {
            CartItemCreateDto createDto = new CartItemCreateDto(
                    testPizza.getId(),
                    testPizzaSizeMedium.getId(),
                    1
            );

            String createDtoJson = objectMapper.writeValueAsString(createDto);

            mockMvc.perform(post("/api/cart")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createDtoJson))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    class UpdateQuantityTests {

        @BeforeEach
        void setupUpdateQuantityTests() {
            // Создаем элемент корзины для тестов обновления
            existingCartItem = createCartItem(customer, testPizza, testPizzaSizeMedium, 2);
        }

        @Test
        void shouldUpdateQuantityCorrectly() throws Exception {
            mockMvc.perform(put("/api/cart")
                            .header("Authorization", customerToken)
                            .param("itemId", existingCartItem.getId().toString())
                            .param("quantity", "5"))
                    .andExpect(status().isOk());

            CartItem updatedItem = cartItemRepository.findById(existingCartItem.getId()).orElseThrow();
            assertEquals(5, updatedItem.getQuantity());
        }

        @Test
        void shouldRemoveItemWhenQuantityIsZero() throws Exception {
            mockMvc.perform(put("/api/cart")
                            .header("Authorization", customerToken)
                            .param("itemId", existingCartItem.getId().toString())
                            .param("quantity", "0"))
                    .andExpect(status().isOk());

            assertFalse(cartItemRepository.existsById(existingCartItem.getId()));
        }

        @Test
        void shouldReturnNotFoundWhenItemNotExists() throws Exception {
            mockMvc.perform(put("/api/cart")
                            .header("Authorization", customerToken)
                            .param("itemId", "999999")
                            .param("quantity", "5"))
                    .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturnBadRequestWhenQuantityIsNegative() throws Exception {
            mockMvc.perform(put("/api/cart")
                            .header("Authorization", customerToken)
                            .param("itemId", existingCartItem.getId().toString())
                            .param("quantity", "-1"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturnBadRequestWhenItemIdIsNull() throws Exception {
            mockMvc.perform(put("/api/cart")
                            .header("Authorization", customerToken)
                            .param("quantity", "5"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturnBadRequestWhenQuantityIsNull() throws Exception {
            mockMvc.perform(put("/api/cart")
                            .header("Authorization", customerToken)
                            .param("itemId", existingCartItem.getId().toString()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturnUnauthorizedWhenNoToken() throws Exception {
            mockMvc.perform(put("/api/cart")
                            .param("itemId", existingCartItem.getId().toString())
                            .param("quantity", "5"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    class ClearCartTests {

        @Autowired
        private EntityManager entityManager;

        @BeforeEach
        void setupClearCartTests() {
            // Добавляем несколько элементов в корзину
            addItemToCart(customer, testPizza, testPizzaSizeMedium, 2);
            addItemToCart(customer, testPizza, testPizzaSizeLarge, 1);
        }

        @Test
        void shouldClearCartSuccessfully() throws Exception {
            // Проверяем, что в корзине есть элементы перед очисткой
            List<CartItem> itemsBefore = cartItemRepository.findByUser(customer);
            assertFalse(itemsBefore.isEmpty());

            mockMvc.perform(delete("/api/cart")
                            .header("Authorization", customerToken))
                    .andExpect(status().isOk());

            // Проверяем, что корзина пуста
            List<CartItem> itemsAfter = cartItemRepository.findByUser(customer);
            assertTrue(itemsAfter.isEmpty());
        }

        @Test
        void shouldClearOnlyCurrentUserCart() throws Exception {
            // Создаем другого пользователя с корзиной
            User anotherUser = User.createUserCustom(
                    "Another Customer",
                    "another@test.com",
                    "+375292223344",
                    "password123",
                    UserRole.CUSTOMER
            );
            User savedAnotherUser = userRepository.save(anotherUser);
            addItemToCart(savedAnotherUser, testPizza, testPizzaSizeMedium, 3);

            // Генерируем токен для другого пользователя
            UserDetailsImpl anotherUserDetails = new UserDetailsImpl(savedAnotherUser);
            Authentication anotherAuth = new UsernamePasswordAuthenticationToken(
                    anotherUserDetails,
                    null,
                    anotherUserDetails.getAuthorities()
            );
            String anotherToken = "Bearer " + jwtUtils.generateJwtToken(anotherAuth);

            // Очищаем корзину другого пользователя
            mockMvc.perform(delete("/api/cart")
                            .header("Authorization", anotherToken))
                    .andExpect(status().isOk());

            // Проверяем, что корзина другого пользователя пуста
            List<CartItem> anotherUserItems = cartItemRepository.findByUser(savedAnotherUser);
            assertTrue(anotherUserItems.isEmpty());

            // Проверяем, что корзина текущего пользователя осталась нетронутой
            List<CartItem> currentUserItems = cartItemRepository.findByUser(customer);
            assertFalse(currentUserItems.isEmpty());
        }

        @Test
        void shouldHandleEmptyCart() throws Exception {
            // Сначала очищаем корзину
            cartItemRepository.findByUser(customer).forEach(item ->
                    cartItemRepository.delete(item)
            );

            mockMvc.perform(delete("/api/cart")
                            .header("Authorization", customerToken))
                    .andExpect(status().isOk());

            List<CartItem> items = cartItemRepository.findByUser(customer);
            assertTrue(items.isEmpty());
        }

    }
}