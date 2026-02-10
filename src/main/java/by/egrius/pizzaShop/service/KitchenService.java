package by.egrius.pizzaShop.service;

import by.egrius.pizzaShop.entity.Order;
import by.egrius.pizzaShop.entity.OrderStatus;
import by.egrius.pizzaShop.event.OrderPaidEvent;
import by.egrius.pizzaShop.exception.OrderNotFoundException;
import by.egrius.pizzaShop.exception.OrderProcessingException;
import by.egrius.pizzaShop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@Slf4j
@RequiredArgsConstructor
public class KitchenService {
    private final OrderRepository orderRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void acceptPaidOrder(OrderPaidEvent orderPaidEvent) {

        Long orderId = orderPaidEvent.getOrderId();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Не найден заказ для обработки на кухне, id заказа: " + orderId));

        if(order.getStatus() != OrderStatus.PAID) {
            throw new OrderProcessingException("Переданный на кухню заказ не имеет статуса оплаты, статус заказа: " + orderId);

        }

        log.info("Заказ c id: " + orderId + " принят на кухню, статус заказа обновлён");

        order.setStatus(OrderStatus.CONFIRMED);
    }

    // Сделать обработку на основе шедулеров
    // каждые 30 секунд собирать список принятых заказов и их готовить
    /*
    у Order будет поле StartedCookingAt условно, и для каждого заказа со статусом COOKING в цикле
    ordersToComplete.forEach брать условно сам заказ, оттуда взять самое долгое время из имеющейся пиццы
    и сравнивать текущее время с StartedCookingAt  + время с пиццы и таким образом если время прошло,
    то менять статус
    */
}
