package by.egrius.pizzaShop.integration.service;

import by.egrius.pizzaShop.dto.order.OrderCreateDto;
import by.egrius.pizzaShop.dto.order.OrderReadDto;
import by.egrius.pizzaShop.entity.*;
import by.egrius.pizzaShop.event.publisher.OrderEventPublisher;
import by.egrius.pizzaShop.exception.EmptyCartException;
import by.egrius.pizzaShop.integration.testcontainer.TestContainerBase;
import by.egrius.pizzaShop.payment_imitation.PaymentDetails;
import by.egrius.pizzaShop.payment_imitation.PaymentResponseDto;
import by.egrius.pizzaShop.payment_imitation.PaymentService;
import by.egrius.pizzaShop.repository.*;
import by.egrius.pizzaShop.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.awaitility.Awaitility.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Интеграционные тесты для OrderService.
 * PaymentService компонент замокан ввиду специфики его логики и асинхрона.
 * Тесты нацелены на покрытие сценариев в зависимости от успеха платежа.
 */

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Sql(scripts = "/test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class OrderServiceUnitIT extends TestContainerBase {
    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PizzaRepository pizzaRepository;

    @Autowired
    private IngredientRepository ingredientRepository;

    @Autowired
    private SizeTemplateRepository sizeTemplateRepository;

    @Autowired
    private PizzaIngredientRepository pizzaIngredientRepository;

    @Autowired
    private PizzaSizeRepository pizzaSizeRepository;

    @Autowired
    private OrderEventPublisher orderEventPublisher;

    @MockitoBean
    private PaymentService paymentService;

    private User customer;
    private User customer2;

    private Pizza TEST_PIZZA;
    private PizzaSize TEST_PIZZA_SIZE_MEDIUM;
    private final PaymentDetails MOCK_PAYMENT_DETAILS = new PaymentDetails("4111111111111111", "TEST USER", "12/30", "123", BigDecimal.valueOf(50.0));

    private final Address MOCK_ADDRESS =  new Address("ул. Тестовая", "1", "1", "1", "Минск");

    @BeforeEach
    void setup() {
        customer = userRepository.save(User.createUserCustom("EgorCustomer", "examlpe@gmail.com",
                "+375291112233", "12345Ab", UserRole.CUSTOMER));

        customer2 = userRepository.save(
                User.createUserCustom("Customer2", "customer2@test.com",
                        "+375291112244", "12345Ab", UserRole.CUSTOMER)
        );

        SizeTemplate sizeTemplate =  SizeTemplate.create(PizzaSizeEnum.MEDIUM, "30см", 30, 650, BigDecimal.ONE);
        sizeTemplateRepository.save(sizeTemplate);

        Ingredient ingredient =  Ingredient.create("Сыр", "Вкусный сыр", new BigDecimal("10.0"), true);
        ingredientRepository.save(ingredient);

        Pizza pizza = Pizza.create(
                "Тестовая пицца",
                "Описание тестовой пиццы",
                "/images/test.jpg",
                "Тестовые",
                true,
                15
        );

        TEST_PIZZA = pizzaRepository.save(pizza);

        PizzaIngredient pizzaIngredient = new PizzaIngredient();
        pizzaIngredient.setPizza(TEST_PIZZA);
        pizzaIngredient.setIngredient(ingredient);
        pizzaIngredient.setWeightGrams(200);

        PizzaIngredient savedPizzaIngredient =  pizzaIngredientRepository.save(pizzaIngredient);

        pizza.setPizzaIngredients(Set.of(savedPizzaIngredient));

        PizzaSize pizzaSize = PizzaSize.create(TEST_PIZZA, sizeTemplate, BigDecimal.valueOf(25.0), true);
        TEST_PIZZA_SIZE_MEDIUM = pizzaSizeRepository.save(pizzaSize);
    }

    private void setupCartItem() {
        CartItem cartItem = new CartItem();
        cartItem.setPizza(TEST_PIZZA);
        cartItem.setUser(customer);
        cartItem.setQuantity(1);
        cartItem.setAddedAt(LocalDateTime.now());
        cartItem.setPizzaSize(TEST_PIZZA_SIZE_MEDIUM);

        cartItemRepository.save(cartItem);
    }

    @Test
    void createOrderFromCart_shouldCreateOrderAndClearCart_whenPaymentSuccessful() {

        setupCartItem();

        // Мок успешного платежа
        when(paymentService.processPaymentWithValidation(any(PaymentDetails.class)))
                .thenReturn(CompletableFuture.completedFuture(
                        PaymentResponseDto.success("TXN_TEST_123", "Payment successful")
                ));

        OrderCreateDto orderCreateDto = new OrderCreateDto(
                MOCK_ADDRESS, "Комментарий", DeliveryType.DELIVERY, MOCK_PAYMENT_DETAILS
        );

        OrderReadDto result = orderService.createOrderFromCart(customer, orderCreateDto);

        assertNotNull(result);
        assertTrue(result.orderNumber().startsWith("ORD-"));

        // Ожидание асинхронной обработки
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            Order order = orderRepository.findById(result.id()).orElseThrow();
            assertEquals(OrderStatus.PAID, order.getStatus());
        });

        Order savedOrder = orderRepository.findById(result.id()).orElseThrow();
        assertTrue(cartItemRepository.findByUser(customer).isEmpty());
    }

    @Test
    void createOrderFromCart_shouldSetPaymentFailedAndKeepCart_whenPaymentFails() {

        setupCartItem();

        // Мок неудачного платежа
        when(paymentService.processPaymentWithValidation(any(PaymentDetails.class)))
                .thenReturn(CompletableFuture.completedFuture(
                        PaymentResponseDto.failed("INSUFFICIENT_FUNDS", "Insufficient funds")
                ));

        OrderCreateDto orderCreateDto = new OrderCreateDto(
                MOCK_ADDRESS, "Комментарий", DeliveryType.DELIVERY, MOCK_PAYMENT_DETAILS
        );

        OrderReadDto result = orderService.createOrderFromCart(customer, orderCreateDto);

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            Order order = orderRepository.findById(result.id()).orElseThrow();
            assertEquals(OrderStatus.PAYMENT_FAILED, order.getStatus());
        });

        assertFalse(cartItemRepository.findByUser(customer).isEmpty());
    }

    @Test
    void createOrderFromCart_shouldThrowEmptyCartException_whenCartEmpty() {
        OrderCreateDto orderCreateDto = new OrderCreateDto(
                MOCK_ADDRESS, null, DeliveryType.IN_STORE, MOCK_PAYMENT_DETAILS
        );

        assertThrows(EmptyCartException.class,
                () -> orderService.createOrderFromCart(customer, orderCreateDto));
    }

    // Изучить внимательно
    @Test
    void createOrderFromCart_shouldIsolateTransactionsBetweenDifferentUsers() throws InterruptedException, ExecutionException, TimeoutException {
        setupCartItem(customer, 2);
        setupCartItem(customer2, 2);

        PaymentDetails details1 = new PaymentDetails(
                "4111111111111111", "John Doe", "12/30", "123", BigDecimal.valueOf(50.0)
        );

        PaymentDetails details2 = new PaymentDetails(
                "5555555555554444", "Jane Smith", "12/30", "456", BigDecimal.valueOf(50.0)
        );

        // Мокаем платежи с разным временем выполнения
        AtomicBoolean customer1PaymentStarted = new AtomicBoolean(false);
        AtomicBoolean customer2PaymentCompleted = new AtomicBoolean(false);

        when(paymentService.processPaymentWithValidation(any(PaymentDetails.class)))
                .thenAnswer(invocation -> {
                    PaymentDetails details = invocation.getArgument(0);

                    if (details.cardHolder().equals("John Doe")) {
                        // Долгий платеж для customer1
                        customer1PaymentStarted.set(true);
                        Thread.sleep(3000); // 3 секунды задержки
                        return CompletableFuture.completedFuture(
                                PaymentResponseDto.success("TXN_SLOW_1", "Slow payment")
                        );
                    } else {
                        // Быстрый платеж для customer2
                        return CompletableFuture.completedFuture(
                                PaymentResponseDto.success("TXN_FAST_2", "Fast payment")
                        ).thenApply(response -> {
                            customer2PaymentCompleted.set(true);
                            return response;
                        });
                    }
                });

        // ACT - Запускаем оба заказа почти одновременно
        ExecutorService executor = Executors.newFixedThreadPool(2);

        Future<?> future1 = executor.submit(() -> {
            OrderCreateDto dto1 = new OrderCreateDto(
                    MOCK_ADDRESS, "Order 1", DeliveryType.DELIVERY, details1
            );
            return orderService.createOrderFromCart(customer, dto1);
        });

        Future<?> future2 = executor.submit(() -> {
            OrderCreateDto dto2 = new OrderCreateDto(
                    MOCK_ADDRESS, "Order 2", DeliveryType.DELIVERY, details2
            );
            return orderService.createOrderFromCart(customer2, dto2);
        });

        // Ждем завершения быстрого платежа
        await().atMost(2, TimeUnit.SECONDS)
                .until(customer2PaymentCompleted::get);

        // В этот момент customer2 должен уже завершить платеж,
        // а customer1 еще должен обрабатываться
        assertTrue(customer1PaymentStarted.get(), "Customer1 payment should have started");
        assertTrue(customer2PaymentCompleted.get(), "Customer2 payment should have completed");

        // Дожидаемся завершения обоих
        future1.get(10, TimeUnit.SECONDS);
        future2.get(10, TimeUnit.SECONDS);
        executor.shutdown();

        Order order1 = orderRepository.findByUser(customer).orElseThrow();
        Order order2 = orderRepository.findByUser(customer2).orElseThrow();

        // Оба должны быть PAID
        assertEquals(OrderStatus.PAID, order1.getStatus());
        assertEquals(OrderStatus.PAID, order2.getStatus());

        // Обе корзины должны быть очищены
        assertTrue(cartItemRepository.findByUser(customer).isEmpty());
        assertTrue(cartItemRepository.findByUser(customer2).isEmpty());
    }

    private void setupCartItem(User user, int quantity) {
        CartItem cartItem = new CartItem();
        cartItem.setPizza(TEST_PIZZA);
        cartItem.setUser(user);
        cartItem.setQuantity(quantity);
        cartItem.setAddedAt(LocalDateTime.now());
        cartItem.setPizzaSize(TEST_PIZZA_SIZE_MEDIUM);
        cartItemRepository.save(cartItem);
    }
}