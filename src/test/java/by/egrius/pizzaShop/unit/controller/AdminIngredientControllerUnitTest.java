package by.egrius.pizzaShop.unit.controller;

import by.egrius.pizzaShop.controller.admin.AdminIngredientController;
import by.egrius.pizzaShop.dto.ingredient.IngredientCreateDto;
import by.egrius.pizzaShop.dto.ingredient.IngredientIdsRequest;
import by.egrius.pizzaShop.dto.ingredient.IngredientReadDto;
import by.egrius.pizzaShop.dto.ingredient.IngredientUpdateDto;
import by.egrius.pizzaShop.service.IngredientService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminIngredientControllerUnitTest {

    @Mock
    private IngredientService ingredientService;

    @InjectMocks
    private AdminIngredientController ingredientController;

    private IngredientReadDto testIngredientReadDto;
    private IngredientCreateDto testIngredientCreateDto;
    private IngredientUpdateDto testIngredientUpdateDto;

    @BeforeEach
    void setUp() {
        testIngredientReadDto = new IngredientReadDto(
                1L,
                "Сыр Моцарелла",
                "Свежий сыр моцарелла",
                new BigDecimal("250.50"),
                true,
                LocalDateTime.now()
        );

        testIngredientCreateDto = new IngredientCreateDto(
                "Сыр Моцарелла",
                "Свежий сыр моцарелла",
                new BigDecimal("250.50"),
                true
        );

        testIngredientUpdateDto = new IngredientUpdateDto(
                "Сыр Моцарелла Обновленный",
                "Обновленное описание сыра",
                new BigDecimal("270.00"),
                true
        );
    }

    @Test
    @DisplayName("GET /api/admin/products/all - успешное получение всех ингредиентов с пагинацией")
    void getAllIngredients_Success() {
        // Arrange
        Page<IngredientReadDto> page = new PageImpl<>(List.of(testIngredientReadDto));
        Pageable pageable = PageRequest.of(0, 20);

        when(ingredientService.getAllIngredients(0,20)).thenReturn(page);

        // Act
        ResponseEntity<Page<IngredientReadDto>> response = ingredientController.getAllIngredients(0, 20);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
        assertEquals(testIngredientReadDto, response.getBody().getContent().get(0));

        verify(ingredientService, times(1)).getAllIngredients(0,20);
    }

    @Test
    @DisplayName("POST /api/admin/products/by-ids - успешное получение ингредиентов по IDs")
    void getIngredientsByIds_Success() {
        // Arrange
        Set<Long> ids = Set.of(1L, 2L, 3L);
        IngredientIdsRequest request = new IngredientIdsRequest(ids);
        List<IngredientReadDto> ingredients = List.of(testIngredientReadDto);

        when(ingredientService.getIngredientsByIds(ids)).thenReturn(ingredients);

        // Act
        ResponseEntity<List<IngredientReadDto>> response =
                ingredientController.getIngredientsByIds(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(testIngredientReadDto, response.getBody().get(0));

        verify(ingredientService, times(1)).getIngredientsByIds(ids);
    }

    @Test
    @DisplayName("POST /api/admin/products/by-ids - пустой список IDs")
    void getIngredientsByIds_EmptyIds_ShouldThrowIllegalArgumentException() {
        // Arrange
        Set<Long> emptyIds = Set.of();
        IngredientIdsRequest request = new IngredientIdsRequest(emptyIds);

        when(ingredientService.getIngredientsByIds(emptyIds))
                .thenThrow(new IllegalArgumentException("Список ID ингредиентов не может быть пустым"));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> ingredientController.getIngredientsByIds(request));

        assertEquals("Список ID ингредиентов не может быть пустым", exception.getMessage());

        verify(ingredientService, times(1)).getIngredientsByIds(emptyIds);
    }

    @Test
    @DisplayName("POST /api/admin/products/by-ids - не найденные ингредиенты")
    void getIngredientsByIds_NotFound_ShouldThrowEntityNotFoundException() {
        // Arrange
        Set<Long> ids = Set.of(999L);
        IngredientIdsRequest request = new IngredientIdsRequest(ids);

        when(ingredientService.getIngredientsByIds(ids))
                .thenThrow(new EntityNotFoundException("Не найдены ингредиенты с ID: [999]"));

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> ingredientController.getIngredientsByIds(request));

        assertTrue(exception.getMessage().contains("Не найдены ингредиенты"));

        verify(ingredientService, times(1)).getIngredientsByIds(ids);
    }

    @Test
    @DisplayName("POST /api/admin/products/create - успешное создание ингредиента")
    void createIngredient_Success() {
        // Arrange
        when(ingredientService.createIngredient(testIngredientCreateDto))
                .thenReturn(testIngredientReadDto);

        // Act
        ResponseEntity<IngredientReadDto> response =
                ingredientController.createIngredient(testIngredientCreateDto);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testIngredientReadDto, response.getBody());

        verify(ingredientService, times(1)).createIngredient(testIngredientCreateDto);
    }

    @Test
    @DisplayName("POST /api/admin/products/create - создание с существующим именем")
    void createIngredient_AlreadyExists_ShouldThrowException() {
        // Arrange
        when(ingredientService.createIngredient(testIngredientCreateDto))
                .thenThrow(new jakarta.persistence.EntityExistsException("Ингредиент уже существует"));

        // Act & Assert
        assertThrows(jakarta.persistence.EntityExistsException.class,
                () -> ingredientController.createIngredient(testIngredientCreateDto));

        verify(ingredientService, times(1)).createIngredient(testIngredientCreateDto);
    }

    @Test
    @DisplayName("PUT /api/admin/products/{id} - успешное обновление ингредиента")
    void updateIngredient_Success() {
        // Arrange
        Long id = 1L;
        IngredientReadDto updatedDto = new IngredientReadDto(
                1L,
                "Сыр Моцарелла Обновленный",
                "Обновленное описание",
                new BigDecimal("270.00"),
                true,
                LocalDateTime.now()
        );

        when(ingredientService.updateIngredient(eq(id), eq(testIngredientUpdateDto)))
                .thenReturn(updatedDto);

        // Act
        ResponseEntity<IngredientReadDto> response =
                ingredientController.updateIngredient(id, testIngredientUpdateDto);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(updatedDto, response.getBody());

        verify(ingredientService, times(1)).updateIngredient(id, testIngredientUpdateDto);
    }

    @Test
    @DisplayName("PUT /api/admin/products/{id} - обновление несуществующего ингредиента")
    void updateIngredient_NotFound_ShouldThrowEntityNotFoundException() {
        // Arrange
        Long id = 999L;

        when(ingredientService.updateIngredient(eq(id), any(IngredientUpdateDto.class)))
                .thenThrow(new EntityNotFoundException("Ингредиент с id - " + id + " не найден в БД"));

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> ingredientController.updateIngredient(id, testIngredientUpdateDto));

        assertEquals("Ингредиент с id - 999 не найден в БД", exception.getMessage());

        verify(ingredientService, times(1)).updateIngredient(id, testIngredientUpdateDto);
    }

    @Test
    @DisplayName("DELETE /api/admin/products/{id} - успешное удаление ингредиента")
    void deleteIngredient_Success() {
        // Arrange
        Long id = 1L;
        doNothing().when(ingredientService).deleteIngredient(id);

        // Act
        ResponseEntity<Void> response = ingredientController.deleteIngredient(id);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());

        verify(ingredientService, times(1)).deleteIngredient(id);
    }

    @Test
    @DisplayName("DELETE /api/admin/products/{id} - удаление используемого ингредиента")
    void deleteIngredient_InUse_ShouldThrowIllegalStateException() {
        // Arrange
        Long id = 1L;
        doThrow(new IllegalStateException("Ингредиент используется в пиццах"))
                .when(ingredientService).deleteIngredient(id);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> ingredientController.deleteIngredient(id));

        assertEquals("Ингредиент используется в пиццах", exception.getMessage());

        verify(ingredientService, times(1)).deleteIngredient(id);
    }

    @Test
    @DisplayName("DELETE /api/admin/products/{id} - удаление несуществующего ингредиента")
    void deleteIngredient_NotFound_ShouldThrowEntityNotFoundException() {
        // Arrange
        Long id = 999L;
        doThrow(new EntityNotFoundException("Ингредиент не найден"))
                .when(ingredientService).deleteIngredient(id);

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> ingredientController.deleteIngredient(id));

        assertEquals("Ингредиент не найден", exception.getMessage());

        verify(ingredientService, times(1)).deleteIngredient(id);
    }
}