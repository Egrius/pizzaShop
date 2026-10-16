package by.egrius.pizzaShop.controller.customer;

import by.egrius.pizzaShop.dto.cart.CartReadDto;
import by.egrius.pizzaShop.dto.cart_item.CartItemCreateDto;
import by.egrius.pizzaShop.security.UserDetailsImpl;
import by.egrius.pizzaShop.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Cart", description = "Customer cart. Requires JWT.")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cart")
@Validated
public class CartController {

    private final CartService cartService;

    @Operation(summary = "Get current user's cart")
    @ApiResponse(responseCode = "200", description = "Cart returned")
    @GetMapping
    public ResponseEntity<CartReadDto> getCart(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(cartService.getCart(userDetails.getUser()));
    }

    @Operation(
            summary = "Add item to cart",
            description = "If the same pizza + size is already in the cart, quantity is incremented."
    )
    @ApiResponse(responseCode = "201", description = "Item added or quantity incremented")
    @PostMapping
    public ResponseEntity<Void> addToCart(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                          @RequestBody @Valid CartItemCreateDto dto) {

        System.out.println("userDetails = " + userDetails);
        System.out.println("userDetails.getUser() = " + (userDetails != null ? userDetails.getUser() : "null"));
        System.out.println("userDetails.getId() = " + (userDetails != null ? userDetails.getId() : "null"));

        cartService.addToCart(userDetails.getUser(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(
            summary = "Update item quantity",
            description = "quantity = 0 removes the item from the cart."
    )
    @ApiResponse(responseCode = "200", description = "Updated")
    @PutMapping
    public ResponseEntity<Void> updateQuantity(@AuthenticationPrincipal UserDetailsImpl userDetails,
                          @NotNull @Positive @RequestParam Long itemId,
                          @NotNull @Min(0) @RequestParam Integer quantity) {

        cartService.updateQuantity(userDetails.getUser(), itemId, quantity);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Operation(summary = "Clear cart")
    @ApiResponse(responseCode = "200", description = "Cart cleared")
    @DeleteMapping
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal UserDetailsImpl userDetails) {

        cartService.clearCart(userDetails.getUser());
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}