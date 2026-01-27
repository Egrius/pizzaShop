package by.egrius.pizzaShop.unit.controller;

import by.egrius.pizzaShop.controller.customer.PublicPizzaController;
import by.egrius.pizzaShop.dto.ingredient.IngredientInfoDto;
import by.egrius.pizzaShop.dto.pizza.PizzaCardDetailsDto;
import by.egrius.pizzaShop.dto.pizza.PizzaCardDto;
import by.egrius.pizzaShop.dto.pizza_size.PizzaSizeInfoDto;
import by.egrius.pizzaShop.dto.size_template.SizeTemplateInfoDto;
import by.egrius.pizzaShop.entity.PizzaSizeEnum;
import by.egrius.pizzaShop.service.PizzaService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PublicPizzaControllerUnitTest {
    @Mock
    private PizzaService pizzaService;

    @InjectMocks
    private PublicPizzaController publicPizzaController;

    private PizzaCardDto pizzaCardDto;
    private PizzaCardDetailsDto pizzaCardDetailsDto;

    @BeforeEach
    void setUp() {
        pizzaCardDto = new PizzaCardDto(
                1L, "Margarita","Pizza description",
                "url/to/image/pizza.png" , "Classic pizza",
                BigDecimal.valueOf(12.99)
        );

        pizzaCardDetailsDto = new PizzaCardDetailsDto(1L,"Margarita","Pizza description",
                "url/to/image/pizza.png",
                "Classic pizza",
                15,
                List.of(new IngredientInfoDto("Tomato") , new IngredientInfoDto("Basil")),
                List.of(new PizzaSizeInfoDto(new SizeTemplateInfoDto(PizzaSizeEnum.MEDIUM, "30cm", 30, 400), BigDecimal.valueOf(25.0))
                )
        );
    }

    @Nested
    @DisplayName("getPizzaCards() tests")
    class GetPizzaCardsTests {

        @Test
        @DisplayName("Should return pizza cards with default pagination")
        void getPizzaCards_shouldReturnPizzaCards_withDefaultPagination() {
            // Given
            Slice<PizzaCardDto> mockSlice = new SliceImpl<>(List.of(pizzaCardDto));
            when(pizzaService.getPizzaCardsSlice(0, 8)).thenReturn(mockSlice);

            // When
            ResponseEntity<Slice<PizzaCardDto>> response =
                    publicPizzaController.getPizzaCards(0, 8);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent().size()).isEqualTo(1);
            assertThat(response.getBody().getContent().get(0).name())
                    .isEqualTo("Margarita");

            verify(pizzaService).getPizzaCardsSlice(0, 8);
            verifyNoMoreInteractions(pizzaService);
        }

        @Test
        @DisplayName("Should return pizza cards with custom pagination")
        void getPizzaCards_shouldReturnPizzaCards_withCustomPagination() {
            // Given
            List<PizzaCardDto> cards = List.of(
                    pizzaCardDto
            );
            Slice<PizzaCardDto> mockSlice = new SliceImpl<>(cards);

            when(pizzaService.getPizzaCardsSlice(1, 4)).thenReturn(mockSlice);

            // When
            ResponseEntity<Slice<PizzaCardDto>> response =
                    publicPizzaController.getPizzaCards(1, 4);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getContent().getFirst().name()).isEqualTo("Margarita");

            verify(pizzaService).getPizzaCardsSlice(1, 4);
        }

        @Test
        @DisplayName("Should return empty slice when no pizzas")
        void getPizzaCards_shouldReturnEmptySlice_whenNoPizzas() {
            // Given
            Slice<PizzaCardDto> emptySlice = new SliceImpl<>(List.of());
            when(pizzaService.getPizzaCardsSlice(0, 8)).thenReturn(emptySlice);

            // When
            ResponseEntity<Slice<PizzaCardDto>> response =
                    publicPizzaController.getPizzaCards(0, 8);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertTrue(response.getBody().getContent().isEmpty());

            verify(pizzaService).getPizzaCardsSlice(0, 8);
        }

        @ParameterizedTest
        @CsvSource({
                "-1, 8",
                "0, 0",
                "0, -5"
        })
        @DisplayName("Should handle invalid pagination parameters")
        void getPizzaCards_shouldHandleInvalidParameters(int page, int pageSize) {

            // Given
            Slice<PizzaCardDto> mockSlice = new SliceImpl<>(List.of(pizzaCardDto));
            when(pizzaService.getPizzaCardsSlice(anyInt(), anyInt())).thenReturn(mockSlice);

            // When & Then
            assertDoesNotThrow(() ->
                    publicPizzaController.getPizzaCards(page, pageSize)
            );
        }
    }

    @Nested
    @DisplayName("getPizzaCardDetails() tests")
    class GetPizzaCardDetailsTests {

        @Test
        @DisplayName("Should return pizza details when pizza exists")
        void getPizzaCardDetails_shouldReturnDetails_whenPizzaExists() {
            // Given
            Long pizzaId = 1L;
            when(pizzaService.getPizzaDetails(pizzaId)).thenReturn(pizzaCardDetailsDto);

            // When
            ResponseEntity<PizzaCardDetailsDto> response =
                    publicPizzaController.getPizzaCardDetails(pizzaId);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().id()).isEqualTo(pizzaId);
            assertThat(response.getBody().name()).isEqualTo("Margarita");
            assertThat(response.getBody().ingredientInfoDtos().size()).isEqualTo(2);

            verify(pizzaService).getPizzaDetails(pizzaId);
        }

        @Test
        @DisplayName("Should throw exception when pizza not found")
        void getPizzaCardDetails_shouldThrowException_whenPizzaNotFound() {
            // Given
            Long nonExistentId = 999L;
            String errorMessage = "Пицца с ID - 999 найдена в бд";

            when(pizzaService.getPizzaDetails(nonExistentId))
                    .thenThrow(new EntityNotFoundException(errorMessage));

            // When & Then
            assertThatThrownBy(() ->
                    publicPizzaController.getPizzaCardDetails(nonExistentId)
            )
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessage(errorMessage);

            verify(pizzaService).getPizzaDetails(nonExistentId);
        }

        @Test
        @DisplayName("Should handle null pizza ID")
        void getPizzaCardDetails_shouldHandleNullId() {
            // Given
            Long nullId = null;

            when(pizzaService.getPizzaDetails(nullId))
                    .thenThrow(new IllegalArgumentException("Pizza ID cannot be null"));

            // When & Then
            assertThatThrownBy(() ->
                    publicPizzaController.getPizzaCardDetails(nullId)
            )
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should return correct details for different pizzas")
        void getPizzaCardDetails_shouldReturnCorrectDetails_forDifferentPizzas() {
            // Given
            PizzaCardDetailsDto pepperoniDetails = new PizzaCardDetailsDto (
                    2L,
                    "Pepperoni",
                    "Spicy pepperoni pizza",
                    "url/to/pepperoni.png",
                    "Classic",
                    15,
                    List.of(),
                    List.of()
            );

            when(pizzaService.getPizzaDetails(2L)).thenReturn(pepperoniDetails);

            // When
            ResponseEntity<PizzaCardDetailsDto> response =
                    publicPizzaController.getPizzaCardDetails(2L);

            // Then
            assertThat(response.getBody().id()).isEqualTo(2L);
            assertThat(response.getBody().name()).isEqualTo("Pepperoni");
        }
    }

    @Test
    @DisplayName("Should verify service interactions count")
    void shouldVerifyServiceInteractionsCount() {
        // Given
        Slice<PizzaCardDto> mockSlice = new SliceImpl<>(List.of(pizzaCardDto));
        when(pizzaService.getPizzaCardsSlice(0, 8)).thenReturn(mockSlice);
        when(pizzaService.getPizzaDetails(1L)).thenReturn(pizzaCardDetailsDto);

        // When
        publicPizzaController.getPizzaCards(0, 8);
        publicPizzaController.getPizzaCardDetails(1L);

        // Then
        verify(pizzaService, times(1)).getPizzaCardsSlice(0, 8);
        verify(pizzaService, times(1)).getPizzaDetails(1L);
        verifyNoMoreInteractions(pizzaService);
    }

    @Test
    @DisplayName("Should handle service returning null")
    void shouldHandleServiceReturningNull() {
        // Given
        when(pizzaService.getPizzaCardsSlice(0, 8)).thenReturn(null);

        // When
        ResponseEntity<Slice<PizzaCardDto>> response =
                publicPizzaController.getPizzaCards(0, 8);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNull();
    }

}