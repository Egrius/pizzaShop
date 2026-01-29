package by.egrius.pizzaShop.controller.admin;

import by.egrius.pizzaShop.dto.pizza.PizzaCreateDto;
import by.egrius.pizzaShop.dto.pizza.PizzaReadDto;
import by.egrius.pizzaShop.dto.pizza.PizzaUpdateDto;
import by.egrius.pizzaShop.dto.pizza.PizzaUpdateResponseDto;
import by.egrius.pizzaShop.service.PizzaService;
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

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@RequestMapping("/api/admin/pizzas")
@Validated
public class AdminPizzaController {
    private final PizzaService pizzaService;
    private final Validator validator;

    @PostMapping("/create")
    public ResponseEntity<PizzaReadDto> createPizza(@RequestBody PizzaCreateDto pizzaCreateDto) {

        Set<ConstraintViolation<PizzaCreateDto>> violations = validator.validate(pizzaCreateDto);
        if(!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        PizzaReadDto pizzaReadDto = pizzaService.createPizza(pizzaCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(pizzaReadDto);
    }
    
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePizza(@PathVariable("id") @NotNull Long pizzaId) {
        pizzaService.deletePizzaById(pizzaId);
        return ResponseEntity.noContent().build();
    }
}