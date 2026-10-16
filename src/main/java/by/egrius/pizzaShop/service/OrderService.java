package by.egrius.pizzaShop.service;

import by.egrius.pizzaShop.dto.order.OrderCreateDto;
import by.egrius.pizzaShop.dto.order.OrderReadDto;
import by.egrius.pizzaShop.entity.*;
import by.egrius.pizzaShop.event.publisher.OrderEventPublisher;
import by.egrius.pizzaShop.exception.EmptyCartException;
import by.egrius.pizzaShop.exception.OrderNotFoundException;
import by.egrius.pizzaShop.exception.OrderProcessingException;
import by.egrius.pizzaShop.mapper.order.OrderReadMapper;
import by.egrius.pizzaShop.payment_imitation.PaymentResponseDto;
import by.egrius.pizzaShop.payment_imitation.PaymentService;
import by.egrius.pizzaShop.repository.CartItemRepository;
import by.egrius.pizzaShop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderReadMapper orderReadMapper;
    private final PaymentService paymentService;

    private final OrderEventPublisher orderEventPublisher;

    @Lazy
    @Autowired
    private OrderService self;

    @Transactional
    public OrderReadDto createOrderFromCart(User user, OrderCreateDto orderCreateDto) {

        // Поиск элементов корзины
        List<CartItem> cartItems = cartItemRepository.findByUser(user);

        if (cartItems.isEmpty()) {
            throw new EmptyCartException("Невозможно создать заказ из пустой корзины");
        }

        if(orderCreateDto.deliveryType().equals(DeliveryType.DELIVERY) && orderCreateDto.deliveryAddress() == null) {
            throw new OrderProcessingException("Невозможно сделать заказ с доставкой без указанного адреса");
        }

        // Создание нового заказа
        Order order = new Order();

        order.setOrderNumber(generateOrderNumber()); // Номер заказа
        order.setUser(user);  // Номер заказа
        order.setDeliveryType(orderCreateDto.deliveryType()); // Тип выдачи
        order.setDeliveryAddress(orderCreateDto.deliveryAddress()); // Адрес
        order.setCustomerNotes(orderCreateDto.customerNotes()); // Примечания пользователя
        order.setStatus(OrderStatus.AWAIT_PAYMENT); // Статус ожидания оплаты

        int maxTimeToWaitOrder = 0; // Максимальное время готовки

        BigDecimal total = BigDecimal.ZERO; // Общая цена заказа

        // Создание OrderItem и привязка к основному заказу
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = OrderItem.builder()
                    .order(order) // Привязка к заказу
                    .pizza(cartItem.getPizza())
                    .pizzaSize(cartItem.getPizzaSize())
                    .quantity(cartItem.getQuantity())
                    .unitPrice(cartItem.getPizzaSize().getPrice())
                    .addedAt(LocalDateTime.now())
                    .build();

            int cookingTimeMinutes = cartItem.getPizza().getCookingTimeMinutes();

            // Установка максимального времени ожидания
            if(cookingTimeMinutes > maxTimeToWaitOrder) maxTimeToWaitOrder = cookingTimeMinutes;

            // Подсчет цены за конкретный OrderItem
            orderItem.calculateSubTotal();

            order.getOrderItemList().add(orderItem);
            total = total.add(orderItem.getSubTotal());
        }

        order.setTotalPrice(total);
        order.setCreatedAt(LocalDateTime.now());
        order.setMaxTimeToWait(maxTimeToWaitOrder);

        // Явное сохранение для получения id ДО коммита
        Order saved = orderRepository.save(order);

        Long orderId = saved.getId();

        OrderReadDto orderReadDto = orderReadMapper.map(order);

        // Имитацию проверки баланса добавить
        paymentService.processPaymentWithValidation(orderCreateDto.paymentDetails())
                .whenComplete((paymentResponseDto, throwable) -> {
                    log.info("-- POS 2");
                    if(throwable != null) {
                        log.error("Обработка платежа завершилась неудачно для заказа c ID: " + orderId, throwable);
                        self.handlePaymentResult(orderId, null, throwable, order.getOrderNumber(), order.getTotalPrice());
                    } else {
                        self.handlePaymentResult(orderId, paymentResponseDto, null, order.getOrderNumber(), order.getTotalPrice());
                    }
                });

        return orderReadDto;
    }

    @Transactional
    public void handlePaymentResult(Long orderId,
                                    PaymentResponseDto paymentResponseDto,
                                    Throwable throwable,
                                    String orderNumber,
                                    BigDecimal totalAmount) {

        log.info("=== handlePaymentResult START ===");
        log.info("Order ID: {}", orderId);
        log.info("PaymentResponseDto: {}", paymentResponseDto);
        log.info("Success: {}", paymentResponseDto != null ? paymentResponseDto.success() : "null");
        log.info("Error code: {}", paymentResponseDto != null ? paymentResponseDto.errorCode() : "null");
        log.info("Message: {}", paymentResponseDto != null ? paymentResponseDto.message() : "null");
        log.info("Throwable: {}", throwable != null ? throwable.getMessage() : "null");

        Order order = orderRepository.findById(orderId).orElseThrow(
                () -> new OrderNotFoundException("Заказ с ID: " + orderId + " не найден"));

        if (throwable != null) {
            log.error("Payment processing failed for order ID: {}", orderId, throwable);
            order.setStatus(OrderStatus.PAYMENT_FAILED);

        } else if (paymentResponseDto != null && paymentResponseDto.success()) {
            log.info("Payment SUCCESSFUL - clearing cart");

            order.setStatus(OrderStatus.PAID);
            cartItemRepository.deleteByUser(order.getUser());

            log.info("Cart cleared for user: {}", order.getUser().getId());

            orderEventPublisher.publishOrderPaidEvent(orderId, order.getOrderNumber(), order.getTotalPrice());

        } else if (paymentResponseDto != null && !paymentResponseDto.success()) {
            log.info("Payment FAILED - keeping cart");

            order.setStatus(OrderStatus.PAYMENT_FAILED);

        } else {
            log.error("Invalid parameters");
            throw new IllegalStateException("Invalid payment result state");
        }

        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        log.info("Order status updated to: {}", order.getStatus());
        log.info("=== handlePaymentResult END ===");
    }


    private String generateOrderNumber() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"));
        String random = String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
        return String.format("ORD-%s-%s", date, random);
    }
}