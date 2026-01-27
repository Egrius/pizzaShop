package by.egrius.pizzaShop.controller.customer;

import by.egrius.pizzaShop.dto.pizza.*;
import by.egrius.pizzaShop.service.PizzaService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/pizzas")
@Validated
public class PublicPizzaController {

    private final PizzaService pizzaService;

    @GetMapping("/cards")
    public ResponseEntity<Slice<PizzaCardDto>> getPizzaCards(@RequestParam(name="page", defaultValue = "0") @Min(0) int page,
                                                             @RequestParam(name="pageSize", defaultValue = "8") @Min(1) @Max(50) int pageSize) {
        Slice<PizzaCardDto> pizzaCardDtoSlice = pizzaService.getPizzaCardsSlice(page, pageSize);
        return ResponseEntity.ok(pizzaCardDtoSlice);
    }

    @GetMapping("/details/{id}")
    public ResponseEntity<PizzaCardDetailsDto> getPizzaCardDetails(@PathVariable("id") @NotNull Long pizzaId) {

        PizzaCardDetailsDto pizzaCardDetailsDto = pizzaService.getPizzaDetails(pizzaId);
        return ResponseEntity.ok(pizzaCardDetailsDto);
    }

}
