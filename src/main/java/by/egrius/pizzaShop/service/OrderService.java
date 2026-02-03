package by.egrius.pizzaShop.service;

import by.egrius.pizzaShop.dto.order.OrderCreateDto;
import by.egrius.pizzaShop.dto.order.OrderReadDto;
import by.egrius.pizzaShop.entity.*;
import by.egrius.pizzaShop.event.publisher.OrderEventPublisher;
import by.egrius.pizzaShop.exception.EmptyCartException;
import by.egrius.pizzaShop.exception.OrderNotFoundException;
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


@Service
@RequiredArgsConstructor
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
        List<CartItem> cartItems = cartItemRepository.findByUser(user);

        if (cartItems.isEmpty()) {
            throw new EmptyCartException("Невозможно создать заказ из пустой корзины");
        }

        Order order = new Order();

        Long orderId = order.getId();

        order.setOrderNumber(generateOrderNumber(orderId));
        order.setUser(user);
        order.setDeliveryType(orderCreateDto.deliveryType());
        order.setDeliveryAddress(orderCreateDto.deliveryAddress());
        order.setCustomerNotes(orderCreateDto.customerNotes());
        order.setStatus(OrderStatus.AWAIT_PAYMENT);

        BigDecimal total = BigDecimal.ZERO;
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .pizza(cartItem.getPizza())
                    .pizzaSize(cartItem.getPizzaSize())
                    .quantity(cartItem.getQuantity())
                    .unitPrice(cartItem.getPizzaSize().getPrice())
                    .addedAt(LocalDateTime.now())
                    .build();

            orderItem.calculateSubTotal();
            order.getOrderItemList().add(orderItem);
            total = total.add(orderItem.getSubTotal());
        }

        order.setTotalPrice(total);
        order.setCreatedAt(LocalDateTime.now());

        orderRepository.save(order);

        OrderReadDto orderReadDto = orderReadMapper.map(order);

        paymentService.processPaymentWithValidation(orderCreateDto.paymentDetails())
                .whenComplete((paymentResponseDto, throwable) -> {
                    if(throwable != null) {
                        log.error("Обработка платежа завершилась неудачно для заказа c ID: " + orderId, throwable);
                        self.handlePaymentResult(orderId, null, throwable, order.getOrderNumber(), order.getTotalPrice());
                    } else {
                        self.handlePaymentResult(orderId, paymentResponseDto, null, order.getOrderNumber(), order.getTotalPrice());
                    }
                });

        // Отправить событие о создании заказа, принять на кухне

        return orderReadDto;
    }

    @Transactional
    public void handlePaymentResult(Long orderId, PaymentResponseDto paymentResponseDto, Throwable throwable, String orderNumber, BigDecimal totalAmount) {
        Order order = orderRepository.findById(orderId).orElseThrow(
                () -> new OrderNotFoundException("Заказ с ID: " + orderId + " не найден в ходе обработки результата платежа"));

        if (throwable != null) {
            log.error("Payment processing failed for order ID: {}", orderId, throwable);
            order.setStatus(OrderStatus.PAYMENT_FAILED);

        } else if (paymentResponseDto != null && paymentResponseDto.success()) {
            log.info("Payment successful for order ID: {}, transaction: {}",
                    orderId, paymentResponseDto.transactionId());

            order.setStatus(OrderStatus.PAID);

            cartItemRepository.deleteByUser(order.getUser());

            log.info("Order {} marked as PAID, cart cleared", orderId);

            // Событие
            orderEventPublisher.publishOrderPaidEvent(orderId, orderNumber, totalAmount);

        } else if (paymentResponseDto != null && !paymentResponseDto.success()) {
            log.warn("Payment declined for order ID: {}: {}",
                    orderId, paymentResponseDto.message());

            order.setStatus(OrderStatus.PAYMENT_FAILED);

        } else {
            log.error("Invalid parameters in handlePaymentResult for order ID: {}. " +
                    "Both paymentResponseDto and throwable are null", orderId);
            throw new IllegalStateException("Invalid payment result state");
        }

        orderRepository.save(order);
    }

    private String generateOrderNumber(Long orderId) {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        return String.format("ORDER-%s-%04d", date, orderId % 10000);
    }
}