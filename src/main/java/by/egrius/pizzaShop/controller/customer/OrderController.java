package by.egrius.pizzaShop.controller.customer;

import by.egrius.pizzaShop.dto.order.OrderCreateDto;
import by.egrius.pizzaShop.dto.order.OrderReadDto;
import by.egrius.pizzaShop.security.UserDetailsImpl;
import by.egrius.pizzaShop.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/order")
@Validated
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderReadDto> createOrderFromCart(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                            @RequestBody @Valid OrderCreateDto orderCreateDto) {
        return ResponseEntity.ok(orderService.createOrderFromCart(userDetails.getUser(), orderCreateDto));
    }
}