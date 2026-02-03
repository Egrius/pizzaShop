package by.egrius.pizzaShop.dto.order;

import by.egrius.pizzaShop.dto.order_item.OrderItemReadDto;
import by.egrius.pizzaShop.dto.user.UserReadDto;
import by.egrius.pizzaShop.entity.Address;
import by.egrius.pizzaShop.entity.DeliveryType;
import by.egrius.pizzaShop.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderReadDto (
    Long id,
    String orderNumber,
    UserReadDto userReadDto,
    OrderStatus orderStatus,
    BigDecimal totalPrice,
    Address address,
    String customerNotes,
    DeliveryType deliveryType,
    List<OrderItemReadDto> orderItemReadDtos,
    LocalDateTime createdAt
) { }