package by.egrius.pizzaShop.service;

import by.egrius.pizzaShop.entity.DeliveryType;
import by.egrius.pizzaShop.entity.Order;
import by.egrius.pizzaShop.entity.OrderStatus;
import by.egrius.pizzaShop.event.OrderPaidEvent;
import by.egrius.pizzaShop.exception.OrderNotFoundException;
import by.egrius.pizzaShop.exception.OrderProcessingException;
import by.egrius.pizzaShop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class KitchenService {
    private final OrderRepository orderRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void acceptPaidOrder(OrderPaidEvent orderPaidEvent) {

        Long orderId = orderPaidEvent.getOrderId();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Не найден заказ для обработки на кухне, id заказа: " + orderId));

        if(order.getStatus() != OrderStatus.PAID) {
            throw new OrderProcessingException("Переданный на кухню заказ не имеет статуса оплаты, статус заказа: " + orderId);

        }

        log.info("Заказ c id: " + orderId + " принят на кухню, статус заказа обновлён");

        order.setStatus(OrderStatus.CONFIRMED);
        Order savedOrder = orderRepository.save(order);
        orderRepository.flush();
        log.info("Заказ сохранен с новым статусом: {}", savedOrder.getStatus());
    }

    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void startCookingConfirmedOrders() {
        List<Order> confirmedOrders = orderRepository.findAllByStatus(OrderStatus.CONFIRMED);
        log.debug("Найдено {} заказов для начала готовки", confirmedOrders.size());

        if(!confirmedOrders.isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            confirmedOrders.forEach(o -> {
                o.setStartedCookingAt(now);
                o.setStatus(OrderStatus.COOKING);
                o.setUpdatedAt(now);
                log.info("Заказ {} начал готовиться (максимальное время: {} мин)",
                        o.getOrderNumber(), o.getMaxTimeToWait());
            });

        }
    }

    @Scheduled(fixedDelay = 10000)
    @Transactional
    public void checkOrdersToComplete() {
        List<Order> cookingOrders = orderRepository.findAllByStatus(OrderStatus.COOKING);

        if(cookingOrders.isEmpty()) {
            return;
        }

        log.debug("Проверяем {} заказов на готовность", cookingOrders.size());
        LocalDateTime now = LocalDateTime.now();

        List<Order> readyOrders = cookingOrders.stream()
                .filter(order -> {
                    LocalDateTime expectedReadyAt = order.getStartedCookingAt()
                            .plusMinutes(1);
                    // Для тестов лучше использовать короткие интервалы.
                    // Для реальной симуляции - .plusMinutes(order.getMaxTimeToWait())
                    return now.isAfter(expectedReadyAt);
                })
                .toList();

        if(!readyOrders.isEmpty()) {
            readyOrders.forEach(order -> {

                if(order.getDeliveryType() == DeliveryType.DELIVERY) {
                    order.setStatus(OrderStatus.DELIVERING);
                } else {
                    order.setStatus(OrderStatus.READY);
                }

                // ДЛЯ ТЕСТА!
                String userEmail = order.getUser().getEmail();

                order.setUpdatedAt(LocalDateTime.now());

                log.info("Заказ {} готов! Время приготовления: {} минут",
                        order.getId(),
                        order.getMaxTimeToWait());


            });

            orderRepository.saveAll(readyOrders);
        }
    }
}