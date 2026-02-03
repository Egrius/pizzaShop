package by.egrius.pizzaShop.mapper.order_item;

import by.egrius.pizzaShop.dto.cart_item.CartItemReadDto;
import by.egrius.pizzaShop.dto.order_item.OrderItemReadDto;
import by.egrius.pizzaShop.entity.CartItem;
import by.egrius.pizzaShop.entity.OrderItem;
import by.egrius.pizzaShop.mapper.BaseMapper;
import by.egrius.pizzaShop.mapper.pizza.PizzaReadMapper;
import by.egrius.pizzaShop.mapper.pizza_size.PizzaSizeReadMapper;
import by.egrius.pizzaShop.mapper.user.UserReadMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderItemReadMapper implements BaseMapper<OrderItem, OrderItemReadDto> {

    @Override
    public OrderItemReadDto map(OrderItem object) {
        return new OrderItemReadDto(
                object.getId(),
                object.getOrder().getId(),
                object.getPizza().getId(),
                object.getPizzaSize().getId(),
                object.getQuantity(),
                object.getUnitPrice(),
                object.getAddedAt(),
                object.getSubTotal()
        );
    }
}
