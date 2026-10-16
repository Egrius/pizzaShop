package by.egrius.pizzaShop.dto.notification;

import by.egrius.pizzaShop.entity.DeliveryType;
import by.egrius.pizzaShop.entity.OrderStatus;

import java.time.LocalDateTime;

public record OrderNotification(
        OrderStatus orderStatus,// ORDER_STATUS
        Long orderId,          // ID заказа
        String orderNumber,    // Номер заказа (для отображения)
        DeliveryType deliveryType,   // Тип доставки
        String message,        // Текст уведомления
        LocalDateTime timestamp      // Время уведомления
) {}