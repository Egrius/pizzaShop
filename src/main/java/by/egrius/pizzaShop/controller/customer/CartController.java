package by.egrius.pizzaShop.controller.customer;

import by.egrius.pizzaShop.dto.cart.CartReadDto;
import by.egrius.pizzaShop.dto.cart_item.CartItemCreateDto;
import by.egrius.pizzaShop.security.UserDetailsImpl;
import by.egrius.pizzaShop.service.CartService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cart")
@Validated
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartReadDto> getCart(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(cartService.getCart(userDetails.getUser()));
    }

    @PostMapping
    public ResponseEntity<Void> addToCart(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                          @RequestBody @Valid CartItemCreateDto dto) {

        cartService.addToCart(userDetails.getUser(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping
    public ResponseEntity<Void> updateQuantity(@AuthenticationPrincipal UserDetailsImpl userDetails,
                          @NotNull @Positive @RequestParam Long itemId,
                          @NotNull @Min(0) @RequestParam Integer quantity) {

        cartService.updateQuantity(userDetails.getUser(), itemId, quantity);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal UserDetailsImpl userDetails) {

        cartService.clearCart(userDetails.getUser());
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}