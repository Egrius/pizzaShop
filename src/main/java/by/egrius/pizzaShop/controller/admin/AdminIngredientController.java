package by.egrius.pizzaShop.controller.admin;

import by.egrius.pizzaShop.dto.ingredient.IngredientCreateDto;
import by.egrius.pizzaShop.dto.ingredient.IngredientIdsRequest;
import by.egrius.pizzaShop.dto.ingredient.IngredientReadDto;
import by.egrius.pizzaShop.dto.ingredient.IngredientUpdateDto;
import by.egrius.pizzaShop.service.IngredientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "Admin - Ingredients",
        description = "ADMIN only. Note: path is /api/admin/products/**")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/admin/products")
@Validated
public class AdminIngredientController {

    private final IngredientService ingredientService;

    @Operation(summary = "List ingredients (paginated)")
    @ApiResponse(responseCode = "200", description = "Page of ingredients")
    @GetMapping("/all")
    public ResponseEntity<Page<IngredientReadDto>> getAllIngredients(@RequestParam(name = "page", defaultValue = "0")
                                                                      @Min(value = 0, message = "Номер страницы не может быть отрицательным")
                                                                      int page,

                                                                      @RequestParam(name = "pageSize", defaultValue = "20")
                                                                      @Min(value = 1, message = "Размер страницы должен быть не менее 1")
                                                                      @Max(value = 100, message = "Размер страницы не может превышать 100")
                                                                      int pageSize) {

        return ResponseEntity.ok(ingredientService.getAllIngredients(page, pageSize));
    }

    @Operation(summary = "Fetch ingredients by IDs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ingredients found"),
            @ApiResponse(responseCode = "400", description = "Empty IDs list"),
            @ApiResponse(responseCode = "404", description = "One or more IDs not found")
    })
    @PostMapping("/by-ids")
    public ResponseEntity<List<IngredientReadDto>> getIngredientsByIds(@Valid @RequestBody IngredientIdsRequest request) {
        return ResponseEntity.ok(ingredientService.getIngredientsByIds(request.ids()));
    }

    @Operation(summary = "Create ingredient", description = "Name must be unique.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Ingredient created"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "409", description = "Ingredient with this name already exists")
    })
    @PostMapping("/create")
    public ResponseEntity<IngredientReadDto> createIngredient(@Valid @RequestBody IngredientCreateDto ingredientCreateDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ingredientService.createIngredient(ingredientCreateDto));
    }

    @Operation(
            summary = "Update ingredient",
            description = """
                If the price changes, publishes `IngredientPriceChangedEvent` -> async
                `REQUIRES_NEW` recalculation runs for every pizza using this ingredient.
                Uses `@Version` optimistic lock - a concurrent update fails with `OptimisticLockingFailureException`.
                """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Updated"),
            @ApiResponse(responseCode = "404", description = "Ingredient not found"),
            @ApiResponse(responseCode = "409", description = "Name conflict with another ingredient")
    })
    @PutMapping("/{id}")
    public ResponseEntity<IngredientReadDto> updateIngredient(@PathVariable(name = "id")
                                                                  @Positive(message = "ID должен быть положительным")
                                                                  Long id,

                                                              @Valid @RequestBody IngredientUpdateDto ingredientUpdateDto) {
        return ResponseEntity.status(HttpStatus.OK).body(ingredientService.updateIngredient(id,ingredientUpdateDto));
    }


    @Operation(
            summary = "Delete ingredient",
            description = "Fails with 409 if the ingredient is used in any pizza."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Deleted"),
            @ApiResponse(responseCode = "404", description = "Ingredient not found"),
            @ApiResponse(responseCode = "409", description = "Ingredient is used in pizzas")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIngredient(@PathVariable("id")
                                                     @Positive(message = "ID должен быть положительным")
                                                     Long id) {
        ingredientService.deleteIngredient(id);
        return ResponseEntity.noContent().build();
    }
}