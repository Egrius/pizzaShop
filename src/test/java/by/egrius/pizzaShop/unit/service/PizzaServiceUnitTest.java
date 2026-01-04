package by.egrius.pizzaShop.unit.service;

import by.egrius.pizzaShop.dto.ingredient.IngredientCreateDto;
import by.egrius.pizzaShop.dto.ingredient.IngredientReadDto;
import by.egrius.pizzaShop.dto.pizza.PizzaCreateDto;
import by.egrius.pizzaShop.dto.pizza.PizzaReadDto;
import by.egrius.pizzaShop.entity.Ingredient;
import by.egrius.pizzaShop.entity.Pizza;
import by.egrius.pizzaShop.entity.PizzaSize;
import by.egrius.pizzaShop.exception.PizzaAlreadyExistsException;
import by.egrius.pizzaShop.mapper.pizza.PizzaCreateMapper;
import by.egrius.pizzaShop.mapper.pizza.PizzaReadMapper;
import by.egrius.pizzaShop.repository.IngredientRepository;
import by.egrius.pizzaShop.repository.PizzaRepository;
import by.egrius.pizzaShop.service.PizzaService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PizzaServiceUnitTest {
    @Mock
    private PizzaRepository pizzaRepository;

    @Mock
    private PizzaCreateMapper pizzaCreateMapper;

    @Mock
    private PizzaReadMapper pizzaReadMapper;

    @Mock
    private IngredientRepository ingredientRepository;

    @InjectMocks
    private PizzaService pizzaService;

    @Nested
    class CreatePizzaTests {

        @Test
        void shouldCreatePizzaAndReturnDto_whenCorrectData() {
            PizzaCreateDto dto = new PizzaCreateDto(
                    "Маргарита",
                    "Классическая пицца",
                    "/images/margherita.jpg",
                    "Классические",
                    true,
                    12,
                    Set.of(1L)
            );

            PizzaReadDto expectedDto = new PizzaReadDto(
                    1L,
                    "Маргарита",
                    "Классическая пицца",
                    "/images/margherita.jpg",
                    "Классическая пицца",
                    true,
                    12,
                    LocalDateTime.now(),

                    List.of(new IngredientReadDto(
                            1L,
                            "Моцарелла",
                            "Сыр моцарелла",
                            new BigDecimal("2.50"),
                            true,
                            LocalDateTime.now()
                    ))
            );

            Ingredient mockIngredient = Mockito.mock(Ingredient.class);

            Pizza mockPizza = Mockito.mock(Pizza.class);

            when(pizzaRepository.existsByName("Маргарита")).thenReturn(false);
            when(ingredientRepository.findAllById(Set.of(1L))).thenReturn(List.of(mockIngredient));
            when(pizzaCreateMapper.map(dto)).thenReturn(mockPizza);
            when(pizzaReadMapper.map(mockPizza)).thenReturn(expectedDto);

            PizzaReadDto actualDto = pizzaService.createPizza(dto);

            assertNotNull(actualDto);
            assertEquals(expectedDto.name(), actualDto.name());

            verify(mockPizza, times(1)).setIngredients(anyList());
            verify(pizzaRepository, times(1)).save(mockPizza);
        }

        @Test
        void createPizzaWithoutIngredientsShouldThrow() {
            PizzaCreateDto createDto = new PizzaCreateDto(
                    "Маргарита",
                    "Классическая пицца",
                    "/images/margherita.jpg",
                    "Классические",
                    true,
                    12,
                    Collections.emptySet()
            );

            assertThatThrownBy(() -> pizzaService.createPizza(createDto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("должна содержать ингредиенты");

        }

        @Test
        void createPizzaShouldThrow_ifNameAlreadyExists() {
            PizzaCreateDto createDto = new PizzaCreateDto(
                    "Маргарита",
                    "Классическая пицца",
                    "/images/margherita.jpg",
                    "Классические",
                    true,
                    12,
                    Set.of(1L)
            );

            when(pizzaRepository.existsByName("Маргарита")).thenReturn(true);

            assertThatThrownBy(() -> pizzaService.createPizza(createDto))
                    .isInstanceOf(PizzaAlreadyExistsException.class)
                    .hasMessageContaining("Пицца с таким именем уже существует");
        }

        @Test
        void createPizzaShouldThrow_whenSomeIngredientsNotFound() {

            Set<Long> ingredientIds =  Set.of(1L, 2L, 3L, 4L);

            PizzaCreateDto createDto = new PizzaCreateDto(
                    "Маргарита",
                    "Классическая пицца",
                    "/images/margherita.jpg",
                    "Классические",
                    true,
                    12,
                    ingredientIds
            );

            when(pizzaRepository.existsByName("Маргарита")).thenReturn(false);

            Ingredient mockIngredient1 = Mockito.mock(Ingredient.class);
            Ingredient mockIngredient2 = Mockito.mock(Ingredient.class);

            when(ingredientRepository.findAllById(ingredientIds)).thenReturn(
                    List.of(mockIngredient1, mockIngredient2));

            when(mockIngredient1.getId()).thenReturn(1L);
            when(mockIngredient2.getId()).thenReturn(3L);

            assertThatThrownBy(() -> pizzaService.createPizza(createDto))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining( "Не найдены ингредиенты с id: 2, 4");
        }
    }
}