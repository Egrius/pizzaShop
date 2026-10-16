package by.egrius.pizzaShop.controller.admin;

import by.egrius.pizzaShop.dto.pizza.PizzaCreateDto;
import by.egrius.pizzaShop.dto.pizza.PizzaReadDto;
import by.egrius.pizzaShop.dto.pizza.PizzaUpdateDto;
import by.egrius.pizzaShop.dto.pizza.PizzaUpdateResponseDto;
import by.egrius.pizzaShop.service.PizzaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@Tag(name = "Admin - Pizzas", description = "ADMIN only")
@SecurityRequirement(name = "bearerAuth")
@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@RequestMapping("/api/admin/pizzas")
@Validated
public class AdminPizzaController {
    private final PizzaService pizzaService;
    private final Validator validator;

    @Operation(
            summary = "Create pizza",
            description = "Ingredients and sizes must be non-empty. Prices are calculated per size by PriceCalculator."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pizza created"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "403", description = "Not ADMIN"),
            @ApiResponse(responseCode = "404", description = "Ingredient or size template not found"),
            @ApiResponse(responseCode = "409", description = "Pizza with same name already exists")
    })
    @PostMapping("/create")
    public ResponseEntity<PizzaReadDto> createPizza(@RequestBody PizzaCreateDto pizzaCreateDto) {

        Set<ConstraintViolation<PizzaCreateDto>> violations = validator.validate(pizzaCreateDto);
        if(!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        PizzaReadDto pizzaReadDto = pizzaService.createPizza(pizzaCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(pizzaReadDto);
    }

    @Operation(summary = "Update pizza", description = "Partial update — only non-null fields are applied.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Updated"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "404", description = "Pizza not found"),
            @ApiResponse(responseCode = "409", description = "Name conflict")
    })
    @PutMapping("/{id}")
    public ResponseEntity<PizzaUpdateResponseDto> updatePizza(@PathVariable("id") @NotNull Long pizzaId,
                                                              @RequestBody PizzaUpdateDto pizzaUpdateDto) {

        Set<ConstraintViolation<PizzaUpdateDto>> violations = validator.validate(pizzaUpdateDto);
        if(!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        PizzaUpdateResponseDto pizzaUpdateResponseDto = pizzaService.updatePizzaById(pizzaId, pizzaUpdateDto);
        return ResponseEntity.status(HttpStatus.OK).body(pizzaUpdateResponseDto);
    }

    @Operation(
            summary = "Delete pizza",
            description = "Cascades to pizza_ingredients and pizza_sizes. Ingredients and size templates are left intact."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Deleted"),
            @ApiResponse(responseCode = "404", description = "Pizza not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePizza(@PathVariable("id") @NotNull Long pizzaId) {
        pizzaService.deletePizzaById(pizzaId);
        return ResponseEntity.noContent().build();
    }
}