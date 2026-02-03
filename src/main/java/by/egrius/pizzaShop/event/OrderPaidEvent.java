package by.egrius.pizzaShop.event;

import by.egrius.pizzaShop.dto.order.OrderReadDto;
import lombok.Getter;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;

@Getter
@ToString
public class OrderPaidEvent extends ApplicationEvent {

    private final Long orderId;
    private final String orderNumber;
    private final BigDecimal totalAmount;

    public OrderPaidEvent(Object source, Long orderId,  String orderNumber, BigDecimal totalAmount) {
        super(source);
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.totalAmount = totalAmount;
    }
}
