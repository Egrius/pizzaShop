package by.egrius.pizzaShop.controller.customer;

import by.egrius.pizzaShop.dto.pizza.*;
import by.egrius.pizzaShop.filter.PizzaFilter;
import by.egrius.pizzaShop.service.PizzaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@Tag(name = "Public Pizzas", description = "Public catalog. No auth required.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/pizzas")
@Validated
public class PublicPizzaController {

    private final PizzaService pizzaService;

    @Operation(
            summary = "Paginated pizza cards",
            description = "Returns a Slice - cheaper for infinite scroll. " +
                    "Sorted by name."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Slice of pizza cards"),
            @ApiResponse(responseCode = "400", description = "page < 0 or pageSize <= 0")
    })
    @GetMapping("/cards")
    public ResponseEntity<Slice<PizzaCardDto>> getPizzaCards(@RequestParam(name="page", defaultValue = "0") @Min(0) int page,
                                                             @RequestParam(name="pageSize", defaultValue = "8") @Min(1) int pageSize) {
        Slice<PizzaCardDto> pizzaCardDtoSlice = pizzaService.getPizzaCardsSlice(page, pageSize);
        return ResponseEntity.ok(pizzaCardDtoSlice);
    }

    @Operation(summary = "Pizza details by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pizza found"),
            @ApiResponse(responseCode = "404", description = "Pizza not found")
    })
    @GetMapping("/details/{id}")
    public ResponseEntity<PizzaCardDetailsDto> getPizzaCardDetails(@PathVariable("id") @NotNull Long pizzaId) {

        PizzaCardDetailsDto pizzaCardDetailsDto = pizzaService.getPizzaDetails(pizzaId);
        return ResponseEntity.ok(pizzaCardDetailsDto);
    }

    @Operation(
            summary = "Filter pizzas",
            description = "Filter by name / category / price range. All filters optional."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Matching pizzas"),
            @ApiResponse(responseCode = "400", description = "Validation failed")
    })
    @PostMapping("/filter")
    public ResponseEntity<List<PizzaCardDto>> getPizzasByFilter(@RequestBody @Valid @NotNull PizzaFilter pizzaFilter) {
        return ResponseEntity.ok(pizzaService.getPizzaCardsByFilter(pizzaFilter));
    }
}