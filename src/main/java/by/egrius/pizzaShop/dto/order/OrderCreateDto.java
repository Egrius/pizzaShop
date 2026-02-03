package by.egrius.pizzaShop.dto.order;

import by.egrius.pizzaShop.dto.order_item.OrderItemCreateDto;
import by.egrius.pizzaShop.entity.Address;
import by.egrius.pizzaShop.entity.DeliveryType;
import by.egrius.pizzaShop.payment_imitation.PaymentDetails;

import java.util.List;

public record OrderCreateDto(
        String orderNumber,
        Address deliveryAddress,
        String customerNotes,
        DeliveryType deliveryType,
        PaymentDetails paymentDetails
) { }
