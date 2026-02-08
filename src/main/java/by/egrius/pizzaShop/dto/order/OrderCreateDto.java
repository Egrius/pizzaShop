package by.egrius.pizzaShop.dto.order;

import by.egrius.pizzaShop.entity.Address;
import by.egrius.pizzaShop.entity.DeliveryType;
import by.egrius.pizzaShop.payment_imitation.PaymentDetails;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;


public record OrderCreateDto(
        @NotNull
        Address deliveryAddress,

        @Length(min = 0, max = 200)
        String customerNotes,

        @NotNull
        DeliveryType deliveryType,

        @NotNull
        PaymentDetails paymentDetails
) { }
