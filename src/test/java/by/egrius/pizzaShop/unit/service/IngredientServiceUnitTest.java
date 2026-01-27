package by.egrius.pizzaShop.unit.service;

import by.egrius.pizzaShop.dto.ingredient.IngredientCreateDto;
import by.egrius.pizzaShop.dto.ingredient.IngredientReadDto;
import by.egrius.pizzaShop.dto.ingredient.IngredientUpdateDto;
import by.egrius.pizzaShop.entity.Ingredient;
import by.egrius.pizzaShop.exception.IngredientAlreadyExistsException;
import by.egrius.pizzaShop.mapper.ingredient.IngredientCreateMapper;
import by.egrius.pizzaShop.mapper.ingredient.IngredientReadMapper;
import by.egrius.pizzaShop.mapper.ingredient.IngredientUpdateMapper;
import by.egrius.pizzaShop.repository.IngredientRepository;
import by.egrius.pizzaShop.repository.PizzaIngredientRepository;
import by.egrius.pizzaShop.service.IngredientService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IngredientServiceUnitTest {
    @Mock
    IngredientRepository ingredientRepository;

    @Mock
    PizzaIngredientRepository pizzaIngredientRepository;

    @Mock
    IngredientReadMapper ingredientReadMapper;

    @Mock
    IngredientCreateMapper ingredientCreateMapper;

    @Mock
    IngredientUpdateMapper ingredientUpdateMapper;

    @InjectMocks
    IngredientService ingredientService;

    @Nested
    class getIngredientsByIdsTests {
        @Test
        void getIngredientsByIds_shouldReturnIngredients_whenAllIdsExist() {
            // given
            Set<Long> ids = Set.of(1L, 2L);

            Ingredient mockIngredient1 =  Mockito.mock(Ingredient.class);
            Ingredient mockIngredient2 =  Mockito.mock(Ingredient.class);

            List<Ingredient> mockIngredients = List.of(mockIngredient1, mockIngredient2);

            when(mockIngredient1.getId()).thenReturn(1L);
            when(mockIngredient2.getId()).thenReturn(2L);

            when(ingredientRepository.findAllById(ids)).thenReturn(mockIngredients);
            when(ingredientReadMapper.map(any())).thenReturn(Mockito.mock(IngredientReadDto.class));

            // when
            List<IngredientReadDto> result = ingredientService.getIngredientsByIds(ids);

            // then
            assertEquals(2, result.size());
            verify(ingredientRepository).findAllById(ids);
        }

        @Test
        void getIngredientsByIds_shouldThrowException_whenEmptySet() {
            // given
            Set<Long> emptySet = Set.of();

            // when & then
            assertThrows(IllegalArgumentException.class,
                    () -> ingredientService.getIngredientsByIds(emptySet));
        }

        @Test
        void getIngredientsByIds_shouldThrowException_whenSomeIdsNotFound() {
            // given
            Set<Long> requestedIds = Set.of(1L, 2L, 999L);

            Ingredient mockIngredient1 =  Mockito.mock(Ingredient.class);
            Ingredient mockIngredient2 =  Mockito.mock(Ingredient.class);

            List<Ingredient> foundIngredients = List.of(
                    mockIngredient1,
                    mockIngredient2
            );

            when(mockIngredient1.getId()).thenReturn(1L);
            when(mockIngredient2.getId()).thenReturn(2L);

            when(ingredientRepository.findAllById(requestedIds))
                    .thenReturn(foundIngredients);

            // when & then
            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                    () -> ingredientService.getIngredientsByIds(requestedIds));

            assertTrue(exception.getMessage().contains("999"));
        }
    }

    @Nested
    class createIngredientTests {

        @Test
        void createIngredient_shouldCreate_whenNameIsUnique() {
            // given
            IngredientCreateDto mockCreateDto = Mockito.mock(IngredientCreateDto.class);

            when(mockCreateDto.name()).thenReturn("Новый ингредиент");

            Ingredient savedIngredient = new Ingredient();

            IngredientReadDto mockReadDto = Mockito.mock(IngredientReadDto.class);

            when(mockReadDto.name()).thenReturn("Новый ингредиент");

            when(ingredientRepository.existsByName(mockCreateDto.name())).thenReturn(false);
            when(ingredientCreateMapper.map(mockCreateDto)).thenReturn(savedIngredient);
            when(ingredientRepository.save(savedIngredient)).thenReturn(savedIngredient);
            when(ingredientReadMapper.map(savedIngredient))
                    .thenReturn(mockReadDto);

            // when
            IngredientReadDto result = ingredientService.createIngredient(mockCreateDto);

            // then
            assertNotNull(result);
            assertEquals("Новый ингредиент", result.name());
            verify(ingredientRepository).save(savedIngredient);
        }

        @Test
        void createIngredient_shouldThrowException_whenNameAlreadyExists() {
            IngredientCreateDto dto = Mockito.mock(IngredientCreateDto.class);

            when(dto.name()).thenReturn("Существующий");

            when(ingredientRepository.existsByName(dto.name())).thenReturn(true);

            assertThrows(IngredientAlreadyExistsException.class,
                    () -> ingredientService.createIngredient(dto));
        }
    }

    @Nested
    class updateIngredientTest {
        @Test
        void updateIngredient_shouldUpdate_whenIngredientExists() {
            // given
            Long id = 1L;
            IngredientUpdateDto mockUpdateDto = Mockito.mock(IngredientUpdateDto.class);


            IngredientReadDto mockReadDto = Mockito.mock(IngredientReadDto.class);

            Ingredient existing = new Ingredient();
            Ingredient updated = new Ingredient();

            when(ingredientRepository.findById(id)).thenReturn(Optional.of(existing));
            when(ingredientRepository.save(existing)).thenReturn(updated);
            when(ingredientReadMapper.map(updated))
                    .thenReturn(mockReadDto);

            // when
            IngredientReadDto result = ingredientService.updateIngredient(id, mockUpdateDto);

            // then
            assertNotNull(result);
            verify(ingredientUpdateMapper).map(mockUpdateDto, existing);
            verify(ingredientRepository).save(existing);
        }

        @Test
        void updateIngredient_shouldThrowException_whenIngredientNotFound() {
            // given
            Long nonExistentId = 999L;
            IngredientUpdateDto dto = Mockito.mock(IngredientUpdateDto.class);

            when(ingredientRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // when & then
            assertThrows(EntityNotFoundException.class,
                    () -> ingredientService.updateIngredient(nonExistentId, dto));
        }
    }

    @Nested
    class deleteIngredientTest {
        @Test
        void deleteIngredient_shouldDelete_whenNotUsedInPizzas() {
            // given
            Long id = 1L;
            Ingredient ingredient = Mockito.mock(Ingredient.class);
            when(ingredient.getName()).thenReturn("Сыр");

            when(ingredientRepository.findById(id)).thenReturn(Optional.of(ingredient));
            when(pizzaIngredientRepository.countPizzaUses(id)).thenReturn(0L);

            // when
            ingredientService.deleteIngredient(id);

            // then
            verify(ingredientRepository).delete(ingredient);
        }

        @Test
        void deleteIngredient_shouldThrowException_whenUsedInPizzas() {
            // given
            Long id = 1L;
            Ingredient ingredient = Mockito.mock(Ingredient.class);
            when(ingredient.getName()).thenReturn("Сыр");

            when(ingredientRepository.findById(id)).thenReturn(Optional.of(ingredient));
            when(pizzaIngredientRepository.countPizzaUses(id)).thenReturn(5L); // Используется в 5 пиццах

            // when & then
            IllegalStateException exception = assertThrows(IllegalStateException.class,
                    () -> ingredientService.deleteIngredient(id));

            assertTrue(exception.getMessage().contains("используется"));
            verify(ingredientRepository, never()).delete(any());
        }
    }
}