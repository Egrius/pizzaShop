package by.egrius.pizzaShop.controller.customer;

import by.egrius.pizzaShop.dto.order.OrderCreateDto;
import by.egrius.pizzaShop.dto.order.OrderReadDto;
import by.egrius.pizzaShop.security.UserDetailsImpl;
import by.egrius.pizzaShop.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Orders", description = "Order creation. Requires JWT.")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;

    @Operation(
            summary = "Create order from cart",
            description = """
                Returns immediately with the order in `AWAIT_PAYMENT`.
                Payment runs asynchronously in a separate thread pool; the order status
                is then updated to `PAID` or `PAYMENT_FAILED`. On success,
                `OrderPaidEvent` is published and the kitchen listener moves the order
                to `CONFIRMED`.
                """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order created, payment started"),
            @ApiResponse(responseCode = "400", description = "Empty cart, or DELIVERY without address"),
            @ApiResponse(responseCode = "403", description = "Not authenticated")
    })
    @PostMapping
    public ResponseEntity<OrderReadDto> createOrderFromCart(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                            @RequestBody @Valid OrderCreateDto orderCreateDto) {
        return ResponseEntity.ok(orderService.createOrderFromCart(userDetails.getUser(), orderCreateDto));
    }
}