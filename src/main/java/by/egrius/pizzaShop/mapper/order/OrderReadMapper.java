package by.egrius.pizzaShop.mapper.order;

import by.egrius.pizzaShop.dto.order.OrderReadDto;
import by.egrius.pizzaShop.entity.Order;
import by.egrius.pizzaShop.mapper.BaseMapper;
import by.egrius.pizzaShop.mapper.order_item.OrderItemReadMapper;
import by.egrius.pizzaShop.mapper.user.UserReadMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderReadMapper implements BaseMapper<Order, OrderReadDto> {

    private final UserReadMapper userReadMapper;
    private final OrderItemReadMapper orderItemReadMapper;

    @Override
    public OrderReadDto map(Order object) {
        return new OrderReadDto(
                object.getId(),
                object.getOrderNumber(),
                userReadMapper.map(object.getUser()),
                object.getStatus(),
                object.getTotalPrice(),
                object.getDeliveryAddress(),
                object.getCustomerNotes(),
                object.getDeliveryType(),
                object.getOrderItemList().stream().map(orderItemReadMapper::map).toList(),
                object.getCreatedAt()
        );
    }
}
