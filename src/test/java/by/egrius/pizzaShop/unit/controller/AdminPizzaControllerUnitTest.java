package by.egrius.pizzaShop.unit.controller;

import by.egrius.pizzaShop.controller.admin.AdminPizzaController;
import by.egrius.pizzaShop.dto.pizza.*;
import by.egrius.pizzaShop.service.PizzaService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminPizzaController Unit Tests")
class AdminPizzaControllerUnitTest {

    @Mock
    private PizzaService pizzaService;

    @InjectMocks
    private AdminPizzaController adminPizzaController;

    private PizzaCreateDto pizzaCreateDto;
    private PizzaReadDto pizzaReadDto;
    private PizzaUpdateDto pizzaUpdateDto;
    private PizzaUpdateResponseDto pizzaUpdateResponseDto;

    @BeforeEach
    void setUp() {
        pizzaCreateDto = new PizzaCreateDto(
                "Margarita",
                "Classic pizza with tomato and cheese",
                "https://example.com/margarita.jpg",
                "CLASSIC",
                true,
                15,
                Map.of(1L, 100, 2L, 150), // ingredientId -> weight in grams
                Set.of(1L, 2L, 3L) // size template IDs
        );

        pizzaReadDto = new PizzaReadDto(
                1L,
                "Margarita",
                "Classic pizza with tomato and cheese",
                "https://example.com/margarita.jpg",
                "CLASSIC",
                true,
                15,
                LocalDateTime.now(),
                List.of(),
                List.of()

        );

        pizzaUpdateDto = new PizzaUpdateDto(
                "Margarita Updated",
                "Updated description with extra cheese",
                "https://example.com/margarita-updated.jpg",
                "SPECIALTY",
                false,
                20
        );

        pizzaUpdateResponseDto = new PizzaUpdateResponseDto(
                1L,
                "Margarita Updated",
                "Updated description with extra cheese",
                "https://example.com/margarita-updated.jpg",
                "SPECIALTY",
                false,
                20,
                LocalDateTime.now().plusDays(2L)
        );
    }

    @Nested
    @DisplayName("createPizza() tests")
    class CreatePizzaTests {

        @Test
        @DisplayName("Should create pizza and return 201 CREATED")
        void createPizza_shouldCreatePizzaAndReturnCreated() {
            // Given
            when(pizzaService.createPizza(any(PizzaCreateDto.class)))
                    .thenReturn(pizzaReadDto);

            // When
            ResponseEntity<PizzaReadDto> response =
                    adminPizzaController.createPizza(pizzaCreateDto);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().id()).isEqualTo(1L);
            assertThat(response.getBody().name()).isEqualTo("Margarita");
            assertThat(response.getBody().cookingTimeMinutes()).isEqualTo(15);

            ArgumentCaptor<PizzaCreateDto> dtoCaptor =
                    ArgumentCaptor.forClass(PizzaCreateDto.class);
            verify(pizzaService).createPizza(dtoCaptor.capture());

            PizzaCreateDto capturedDto = dtoCaptor.getValue();
            assertThat(capturedDto.name()).isEqualTo("Margarita");
            assertThat(capturedDto.description()).contains("Classic pizza");
            assertThat(capturedDto.ingredientWeights()).hasSize(2);
            assertThat(capturedDto.sizeTemplateIds()).hasSize(3);
        }

        @Test
        @DisplayName("Should handle service returning null")
        void createPizza_shouldHandleNullResponse() {
            // Given
            when(pizzaService.createPizza(any(PizzaCreateDto.class)))
                    .thenReturn(null);

            // When
            ResponseEntity<PizzaReadDto> response =
                    adminPizzaController.createPizza(pizzaCreateDto);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNull();

            verify(pizzaService).createPizza(pizzaCreateDto);
        }

        @Test
        @DisplayName("Should capture PizzaCreateDto correctly")
        void createPizza_shouldCaptureDtoCorrectly() {
            // Given
            PizzaCreateDto complexDto = new PizzaCreateDto(
                    "Pepperoni",
                    "Spicy pepperoni pizza",
                    "https://example.com/pepperoni.jpg",
                    "SPICY",
                    true,
                    18,
                    Map.of(1L, 120, 3L, 80, 5L, 200),
                    Set.of(2L, 4L)
            );

            when(pizzaService.createPizza(any(PizzaCreateDto.class)))
                    .thenReturn(pizzaReadDto);

            // When
            adminPizzaController.createPizza(complexDto);

            // Then
            ArgumentCaptor<PizzaCreateDto> captor =
                    ArgumentCaptor.forClass(PizzaCreateDto.class);
            verify(pizzaService).createPizza(captor.capture());

            PizzaCreateDto captured = captor.getValue();
            assertThat(captured.name()).isEqualTo("Pepperoni");
            assertThat(captured.category()).isEqualTo("SPICY");
            assertThat(captured.cookingTimeMinutes()).isEqualTo(18);
            assertThat(captured.ingredientWeights()).containsEntry(5L, 200);
            assertThat(captured.sizeTemplateIds()).containsExactlyInAnyOrder(2L, 4L);
        }
    }

    @Nested
    @DisplayName("updatePizza() tests")
    class UpdatePizzaTests {

        @Test
        @DisplayName("Should update pizza and return 200 OK")
        void updatePizza_shouldUpdatePizzaAndReturnOk() {
            // Given
            Long pizzaId = 1L;
            when(pizzaService.updatePizzaById(eq(pizzaId), any(PizzaUpdateDto.class)))
                    .thenReturn(pizzaUpdateResponseDto);

            // When
            ResponseEntity<PizzaUpdateResponseDto> response =
                    adminPizzaController.updatePizza(pizzaId, pizzaUpdateDto);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().id()).isEqualTo(pizzaId);
            assertThat(response.getBody().name()).isEqualTo("Margarita Updated");
            assertThat(response.getBody().isAvailable()).isFalse();

            verify(pizzaService).updatePizzaById(pizzaId, pizzaUpdateDto);
        }

        @Test
        @DisplayName("Should handle partial update (only some fields)")
        void updatePizza_shouldHandlePartialUpdate() {
            // Given
            Long pizzaId = 1L;
            PizzaUpdateDto partialUpdateDto = new PizzaUpdateDto(
                    null,
                    "New description only",
                    null,
                    null,
                    false,
                    null
            );

            PizzaUpdateResponseDto partialResponse = new PizzaUpdateResponseDto(
                    1L,
                    "Margarita",
                    "New description only",
                    "https://example.com/margarita.jpg",
                    "CLASSIC",
                    false,
                    15,
                    LocalDateTime.now()
            );

            when(pizzaService.updatePizzaById(eq(pizzaId), eq(partialUpdateDto)))
                    .thenReturn(partialResponse);

            // When
            ResponseEntity<PizzaUpdateResponseDto> response =
                    adminPizzaController.updatePizza(pizzaId, partialUpdateDto);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().name()).isEqualTo("Margarita");
            assertThat(response.getBody().description()).isEqualTo("New description only");
            assertThat(response.getBody().isAvailable()).isFalse();
        }

        @Test
        @DisplayName("Should throw exception when pizza not found")
        void updatePizza_shouldThrowException_whenPizzaNotFound() {
            // Given
            Long nonExistentId = 999L;
            String errorMessage = "Пицца с ID - 999 не найдена в бд";

            when(pizzaService.updatePizzaById(eq(nonExistentId), any(PizzaUpdateDto.class)))
                    .thenThrow(new EntityNotFoundException(errorMessage));

            // When & Then
            assertThatThrownBy(() ->
                    adminPizzaController.updatePizza(nonExistentId, pizzaUpdateDto)
            )
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage(errorMessage);

            verify(pizzaService).updatePizzaById(nonExistentId, pizzaUpdateDto);
        }

        @Test
        @DisplayName("Should handle null pizza ID")
        void updatePizza_shouldHandleNullId() {
            // Given
            Long nullId = null;

            when(pizzaService.updatePizzaById(eq(nullId), any(PizzaUpdateDto.class)))
                    .thenThrow(new IllegalArgumentException("Pizza ID cannot be null"));

            // When & Then
            assertThatThrownBy(() ->
                    adminPizzaController.updatePizza(nullId, pizzaUpdateDto)
            )
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should update all fields when all provided")
        void updatePizza_shouldUpdateAllFields_whenAllProvided() {
            // Given
            Long pizzaId = 1L;
            PizzaUpdateDto fullUpdateDto = new PizzaUpdateDto(
                    "New Name",
                    "New Description",
                    "https://new-image.com/pizza.jpg",
                    "NEW_CATEGORY",
                    true,
                    25
            );

            PizzaUpdateResponseDto fullResponse = new PizzaUpdateResponseDto(
                    pizzaId,
                    "New Name",
                    "New Description",
                    "https://new-image.com/pizza.jpg",
                    "NEW_CATEGORY",
                    true,
                    25,
                    LocalDateTime.now()
            );

            when(pizzaService.updatePizzaById(pizzaId, fullUpdateDto))
                    .thenReturn(fullResponse);

            // When
            ResponseEntity<PizzaUpdateResponseDto> response =
                    adminPizzaController.updatePizza(pizzaId, fullUpdateDto);

            // Then
            PizzaUpdateResponseDto result = response.getBody();
            assertThat(result.id()).isEqualTo(pizzaId);
            assertThat(result.name()).isEqualTo("New Name");
            assertThat(result.description()).isEqualTo("New Description");
            assertThat(result.imageUrl()).isEqualTo("https://new-image.com/pizza.jpg");
            assertThat(result.category()).isEqualTo("NEW_CATEGORY");
            assertThat(result.isAvailable()).isTrue();
            assertThat(result.cookingTimeMinutes()).isEqualTo(25);
        }
    }

    @Nested
    @DisplayName("deletePizza() tests")
    class DeletePizzaTests {

        @Test
        @DisplayName("Should delete pizza and return 204 NO CONTENT")
        void deletePizza_shouldDeletePizzaAndReturnNoContent() {
            // Given
            Long pizzaId = 1L;
            doNothing().when(pizzaService).deletePizzaById(pizzaId);

            // When
            ResponseEntity<Void> response = adminPizzaController.deletePizza(pizzaId);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            assertThat(response.getBody()).isNull();

            verify(pizzaService).deletePizzaById(pizzaId);
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent pizza")
        void deletePizza_shouldThrowException_whenPizzaNotFound() {
            // Given
            Long nonExistentId = 999L;
            String errorMessage = "Пицца с ID - 999 не найдена в бд";

            doThrow(new EntityNotFoundException(errorMessage))
                    .when(pizzaService).deletePizzaById(nonExistentId);

            // When & Then
            assertThatThrownBy(() ->
                    adminPizzaController.deletePizza(nonExistentId)
            )
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage(errorMessage);

            verify(pizzaService).deletePizzaById(nonExistentId);
        }

        @Test
        @DisplayName("Should handle null pizza ID")
        void deletePizza_shouldHandleNullId() {
            // Given
            Long nullId = null;

            doThrow(new IllegalArgumentException("Pizza ID cannot be null"))
                    .when(pizzaService).deletePizzaById(nullId);

            // When & Then
            assertThatThrownBy(() ->
                    adminPizzaController.deletePizza(nullId)
            )
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should verify delete operation")
        void deletePizza_shouldVerifyDeleteOperation() {
            // Given
            Long pizzaId = 1L;

            // When
            adminPizzaController.deletePizza(pizzaId);

            // Then
            verify(pizzaService, times(1)).deletePizzaById(pizzaId);
            verifyNoMoreInteractions(pizzaService);
        }
    }

    @Nested
    @DisplayName("Controller interaction tests")
    class ControllerInteractionTests {

        @Test
        @DisplayName("Should handle all CRUD operations in sequence")
        void shouldHandleAllCrudOperationsInSequence() {
            // Given
            Long pizzaId = 1L;

            when(pizzaService.createPizza(any(PizzaCreateDto.class)))
                    .thenReturn(pizzaReadDto);
            when(pizzaService.updatePizzaById(eq(pizzaId), any(PizzaUpdateDto.class)))
                    .thenReturn(pizzaUpdateResponseDto);
            doNothing().when(pizzaService).deletePizzaById(pizzaId);

            // When
            adminPizzaController.createPizza(pizzaCreateDto);
            adminPizzaController.updatePizza(pizzaId, pizzaUpdateDto);
            adminPizzaController.deletePizza(pizzaId);

            // Then
            verify(pizzaService, times(1)).createPizza(pizzaCreateDto);
            verify(pizzaService, times(1)).updatePizzaById(pizzaId, pizzaUpdateDto);
            verify(pizzaService, times(1)).deletePizzaById(pizzaId);
            verifyNoMoreInteractions(pizzaService);
        }

        @Test
        @DisplayName("Should handle multiple pizza creations")
        void shouldHandleMultiplePizzaCreations() {
            // Given
            PizzaCreateDto secondPizza = new PizzaCreateDto(
                    "Pepperoni",
                    "Spicy pepperoni pizza",
                    "https://example.com/pepperoni.jpg",
                    "SPICY",
                    true,
                    18,
                    Map.of(1L, 120),
                    Set.of(1L)
            );

            PizzaReadDto secondPizzaResponse = new PizzaReadDto(
                    2L,
                    "Pepperoni",
                    "Spicy pepperoni pizza",
                    "https://example.com/pepperoni.jpg",
                    "SPICY",
                    true,
                    18,
                    LocalDateTime.now(),
                    List.of(),
                    List.of()
            );

            when(pizzaService.createPizza(pizzaCreateDto))
                    .thenReturn(pizzaReadDto);
            when(pizzaService.createPizza(secondPizza))
                    .thenReturn(secondPizzaResponse);

            // When
            ResponseEntity<PizzaReadDto> response1 =
                    adminPizzaController.createPizza(pizzaCreateDto);
            ResponseEntity<PizzaReadDto> response2 =
                    adminPizzaController.createPizza(secondPizza);

            // Then
            assertThat(response1.getBody().id()).isEqualTo(1L);
            assertThat(response1.getBody().name()).isEqualTo("Margarita");

            assertThat(response2.getBody().id()).isEqualTo(2L);
            assertThat(response2.getBody().name()).isEqualTo("Pepperoni");

            verify(pizzaService, times(2)).createPizza(any(PizzaCreateDto.class));
        }
    }

    @Nested
    @DisplayName("Error handling tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should propagate service exceptions")
        void shouldPropagateServiceExceptions() {
            Long pizzaId = 1L;
            String serviceErrorMessage = "Database connection failed";

            when(pizzaService.createPizza(any(PizzaCreateDto.class)))
                    .thenThrow(new RuntimeException(serviceErrorMessage));
            when(pizzaService.updatePizzaById(eq(pizzaId), any(PizzaUpdateDto.class)))
                    .thenThrow(new RuntimeException(serviceErrorMessage));
            doThrow(new RuntimeException(serviceErrorMessage))
                    .when(pizzaService).deletePizzaById(pizzaId);

            assertThatThrownBy(() -> adminPizzaController.createPizza(pizzaCreateDto))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage(serviceErrorMessage);

            assertThatThrownBy(() -> adminPizzaController.updatePizza(pizzaId, pizzaUpdateDto))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage(serviceErrorMessage);

            assertThatThrownBy(() -> adminPizzaController.deletePizza(pizzaId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage(serviceErrorMessage);
        }

        @Test
        @DisplayName("Should handle validation exceptions from service")
        void shouldHandleValidationExceptions() {

            String validationError = "Ingredient weight must be positive";

            when(pizzaService.createPizza(any(PizzaCreateDto.class)))
                    .thenThrow(new IllegalArgumentException(validationError));

            assertThatThrownBy(() ->
                    adminPizzaController.createPizza(pizzaCreateDto)
            )
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage(validationError);
        }
    }
}