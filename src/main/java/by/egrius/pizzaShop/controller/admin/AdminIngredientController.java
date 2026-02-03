package by.egrius.pizzaShop.controller.admin;

import by.egrius.pizzaShop.dto.ingredient.IngredientCreateDto;
import by.egrius.pizzaShop.dto.ingredient.IngredientIdsRequest;
import by.egrius.pizzaShop.dto.ingredient.IngredientReadDto;
import by.egrius.pizzaShop.dto.ingredient.IngredientUpdateDto;
import by.egrius.pizzaShop.service.IngredientService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/admin/products")
@Validated
public class AdminIngredientController {

    private final IngredientService ingredientService;

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

    @PostMapping("/by-ids")
    public ResponseEntity<List<IngredientReadDto>> getIngredientsByIds(@Valid @RequestBody IngredientIdsRequest request) {
        return ResponseEntity.ok(ingredientService.getIngredientsByIds(request.ids()));
    }

    @PostMapping("/create")
    public ResponseEntity<IngredientReadDto> createIngredient(@Valid @RequestBody IngredientCreateDto ingredientCreateDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ingredientService.createIngredient(ingredientCreateDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<IngredientReadDto> updateIngredient(@PathVariable(name = "id")
                                                                  @Positive(message = "ID должен быть положительным")
                                                                  Long id,

                                                              @Valid @RequestBody IngredientUpdateDto ingredientUpdateDto) {
        return ResponseEntity.status(HttpStatus.OK).body(ingredientService.updateIngredient(id,ingredientUpdateDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIngredient(@PathVariable("id")
                                                     @Positive(message = "ID должен быть положительным")
                                                     Long id) {
        ingredientService.deleteIngredient(id);
        return ResponseEntity.noContent().build();
    }
}