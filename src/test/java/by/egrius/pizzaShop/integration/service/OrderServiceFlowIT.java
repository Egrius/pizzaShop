package by.egrius.pizzaShop.integration.service;

import by.egrius.pizzaShop.dto.order.OrderCreateDto;
import by.egrius.pizzaShop.dto.order.OrderReadDto;
import by.egrius.pizzaShop.entity.*;
import by.egrius.pizzaShop.event.publisher.OrderEventPublisher;
import by.egrius.pizzaShop.integration.testcontainer.TestContainerBase;
import by.egrius.pizzaShop.payment_imitation.PaymentDetails;
import by.egrius.pizzaShop.payment_imitation.PaymentService;
import by.egrius.pizzaShop.repository.*;
import by.egrius.pizzaShop.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Slf4j
@Sql(scripts = "/test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class OrderServiceFlowIT extends TestContainerBase {

    @Autowired
    private OrderService orderService;

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
    private UserRepository userRepository;

    @Autowired
    private PaymentService paymentService;

    private User customer;

    private Pizza TEST_PIZZA;
    private PizzaSize TEST_PIZZA_SIZE_MEDIUM;

    private final Address MOCK_ADDRESS =  new Address("ул. Тестовая", "1", "1", "1", "Минск");
    private final PaymentDetails MOCK_PAYMENT_DETAILS = new PaymentDetails("4111111111111111", "TEST USER", "12/30", "123", BigDecimal.valueOf(50.0));

    @BeforeEach
    void setup() {
        customer = userRepository.save(User.createUserCustom("EgorCustomer", "examlpe@gmail.com",
                "+375291112233", "12345Ab", UserRole.CUSTOMER));

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
    void createOrderFromCart_shouldProcessFullPaymentFlow_withRealPaymentService() {

        setupCartItem();

        // Используем разные тестовые данные для разных сценариев
        PaymentDetails paymentDetails = new PaymentDetails(
                "4111111111111111", // Карта с вероятностью успеха
                "TEST USER",
                "12/30",
                "123",
                BigDecimal.valueOf(25.0) // Маленькая сумма для теста
        );

        OrderCreateDto orderCreateDto = new OrderCreateDto(
                MOCK_ADDRESS, "Тестовый заказ", DeliveryType.DELIVERY, paymentDetails
        );

        // ACT
        OrderReadDto result = orderService.createOrderFromCart(customer, orderCreateDto);

        // ASSERT - проверяем полный флоу с реальным платежом
        assertNotNull(result);
        assertTrue(result.orderNumber().startsWith("ORD-"));

        // Завершение платежа (реального!)
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            Order order = orderRepository.findById(result.id()).orElseThrow();
            assertTrue(
                    order.getStatus() == OrderStatus.PAID ||
                            order.getStatus() == OrderStatus.PAYMENT_FAILED,
                    "У заказа должен быть один из конечных статусов"
            );
        });

        Order finalOrder = orderRepository.findById(result.id()).orElseThrow();

        // В зависимости от результата разные сценарии
        if (finalOrder.getStatus() == OrderStatus.PAID) {
            log.info("Payment SUCCESSFUL - testing success flow");
            assertTrue(cartItemRepository.findByUser(customer).isEmpty(),
                    "Корзина должна быть очищена после удачного платежа");

            verify(orderEventPublisher).publishOrderPaidEvent(any(Long.class), anyString(), any(BigDecimal.class));
        } else {
            log.info("Payment FAILED - testing failure flow");
            assertFalse(cartItemRepository.findByUser(customer).isEmpty(),
                    "Корзина должна остаться после неудачного платежа");

            verifyNoInteractions(orderEventPublisher);
        }
    }

    @Test
    void createOrderFromCart_shouldHandleConcurrentOrders() throws InterruptedException {
        // Создаем второго пользователя
        User customer2 = userRepository.save(
                User.createUserCustom("SecondCustomer", "second@test.com",
                        "+375291112244", "12345Ab", UserRole.CUSTOMER)
        );

        // Корзины для обоих пользователей
        setupCartItemForUser(customer, TEST_PIZZA, TEST_PIZZA_SIZE_MEDIUM, 2);
        setupCartItemForUser(customer2, TEST_PIZZA, TEST_PIZZA_SIZE_MEDIUM, 1);

        Thread thread1 = new Thread(() -> {
            OrderCreateDto order1 = new OrderCreateDto(
                    MOCK_ADDRESS, "Order 1", DeliveryType.DELIVERY,
                    new PaymentDetails("4111111111111111", "User 1", "12/30", "123", BigDecimal.valueOf(50.0))
            );
            orderService.createOrderFromCart(customer, order1);
        });

        Thread thread2 = new Thread(() -> {
            OrderCreateDto order2 = new OrderCreateDto(
                    MOCK_ADDRESS, "Order 2", DeliveryType.DELIVERY,
                    new PaymentDetails("5555555555554444", "User 2", "12/30", "123", BigDecimal.valueOf(25.0))
            );
            orderService.createOrderFromCart(customer2, order2);
        });

        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();

        // Проверяем, что оба заказа обработаны
        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            List<Order> orders = orderRepository.findAll();
            assertEquals(2, orders.size());
            orders.forEach(order -> {
                assertTrue(
                        order.getStatus() == OrderStatus.PAID ||
                                order.getStatus() == OrderStatus.PAYMENT_FAILED,
                        "All orders should have final status"
                );
            });
        });
    }

    private void setupCartItemForUser(User user, Pizza pizza, PizzaSize pizzaSize, int quantity) {
        CartItem cartItem = new CartItem();
        cartItem.setPizza(pizza);
        cartItem.setUser(user);
        cartItem.setQuantity(quantity);
        cartItem.setAddedAt(LocalDateTime.now());
        cartItem.setPizzaSize(pizzaSize);
        cartItemRepository.save(cartItem);
    }

    @Test
    void createOrderFromCart_shouldHandlePaymentInterruption() throws Exception {
        setupCartItem();

        OrderCreateDto orderCreateDto = new OrderCreateDto(
                MOCK_ADDRESS, "Тестовый заказ с прерыванием", DeliveryType.DELIVERY, MOCK_PAYMENT_DETAILS
        );

        OrderReadDto result = orderService.createOrderFromCart(customer, orderCreateDto);
        assertNotNull(result);

        // Ждем немного чтобы платеж начал обрабатываться в потоке payment-1
        Thread.sleep(1000);

        Set<Thread> paymentThreads = Thread.getAllStackTraces().keySet().stream()
                .filter(t -> t.getName().startsWith("payment-"))
                .collect(Collectors.toSet());

        // Прерываем все потоки payment-*
        for (Thread paymentThread : paymentThreads) {
            log.info("Interrupting payment thread: {}", paymentThread.getName());
            paymentThread.interrupt();
        }

        // Ждем обработки статуса (должен стать PAYMENT_FAILED)
        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            Order order = orderRepository.findById(result.id()).orElseThrow();

            // Проверяем статус
            assertEquals(OrderStatus.PAYMENT_FAILED, order.getStatus(),
                    "Заказ должен иметь статус PAYMENT_FAILED после прерывания платежа");

            // Проверяем, что корзина не очищена
            assertFalse(cartItemRepository.findByUser(customer).isEmpty(),
                    "Корзина не должна быть очищена после неудачного платежа");
        });

        // Дополнительные проверки
        Order finalOrder = orderRepository.findById(result.id()).orElseThrow();
        log.info("Заказ {} имеет статус {}",
                finalOrder.getOrderNumber(), finalOrder.getStatus());

        verify(orderEventPublisher, never()).publishOrderPaidEvent(anyLong(), anyString(), any(BigDecimal.class));
    }
}