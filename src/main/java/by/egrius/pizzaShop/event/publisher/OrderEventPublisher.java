package by.egrius.pizzaShop.event.publisher;

import by.egrius.pizzaShop.event.OrderPaidEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public void publishOrderPaidEvent(Long orderId, String orderNumber, BigDecimal totalAmount) {
        OrderPaidEvent event = new OrderPaidEvent(this, orderId, orderNumber, totalAmount);
        eventPublisher.publishEvent(event);
    }
}
