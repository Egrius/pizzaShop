package by.egrius.pizzaShop.mapper.cart_item;

import by.egrius.pizzaShop.dto.cart_item.CartItemReadDto;
import by.egrius.pizzaShop.entity.CartItem;
import by.egrius.pizzaShop.mapper.BaseMapper;
import by.egrius.pizzaShop.mapper.pizza.PizzaReadMapper;
import by.egrius.pizzaShop.mapper.pizza_size.PizzaSizeReadMapper;
import by.egrius.pizzaShop.mapper.user.UserReadMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CartItemReadMapper implements BaseMapper<CartItem, CartItemReadDto> {

    private final UserReadMapper userReadMapper;
    private final PizzaReadMapper pizzaReadMapper;
    private final PizzaSizeReadMapper pizzaSizeReadMapper;

    @Override
    public CartItemReadDto map(CartItem object) {
        return new CartItemReadDto(
                object.getId(),
                userReadMapper.map(object.getUser()),
                pizzaReadMapper.map(object.getPizza()),
                pizzaSizeReadMapper.map(object.getPizzaSize()),
                object.getQuantity(),
                object.getAddedAt()
        );
    }
}
