package by.egrius.pizzaShop.unit.service;

import by.egrius.pizzaShop.dto.pizza.*;
import by.egrius.pizzaShop.dto.pizza_size.PizzaSizeInfoDto;
import by.egrius.pizzaShop.dto.size_template.SizeTemplateInfoDto;
import by.egrius.pizzaShop.entity.*;
import by.egrius.pizzaShop.exception.PizzaAlreadyExistsException;
import by.egrius.pizzaShop.mapper.pizza.PizzaCreateMapper;
import by.egrius.pizzaShop.mapper.pizza.PizzaReadMapper;
import by.egrius.pizzaShop.mapper.pizza.PizzaUpdateMapper;
import by.egrius.pizzaShop.repository.IngredientRepository;
import by.egrius.pizzaShop.repository.PizzaRepository;
import by.egrius.pizzaShop.repository.SizeTemplateRepository;
import by.egrius.pizzaShop.service.PizzaService;
import by.egrius.pizzaShop.service.PriceCalculator;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

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

    @Mock
    private PriceCalculator priceCalculator;

    @Mock
    private SizeTemplateRepository sizeTemplateRepository;

    @Mock
    private PizzaUpdateMapper pizzaUpdateMapper;

    @InjectMocks
    private PizzaService pizzaService;

    @Nested
    class getPizzaCardsSliceTests {
        @Test
        void shouldReturnCorrectData_whenPizzaFound() {
            PizzaRepository.PizzaCardProjection mockProjection1 = Mockito.mock(PizzaRepository.PizzaCardProjection.class);
            PizzaRepository.PizzaCardProjection mockProjection2 = Mockito.mock(PizzaRepository.PizzaCardProjection.class);
            PizzaRepository.PizzaCardProjection mockProjection3 = Mockito.mock(PizzaRepository.PizzaCardProjection.class);

            Pageable mockPageble = PageRequest.of(1,3);

            Slice<PizzaRepository.PizzaCardProjection> mockSlice = new SliceImpl<>(
                    List.of(mockProjection1, mockProjection2, mockProjection3),
                    mockPageble,
                    false

            );

            when(pizzaRepository.findAvailablePizzasForCards(mockPageble)).thenReturn(mockSlice);

            when(mockProjection1.getId()).thenReturn(1L);
            when(mockProjection1.getName()).thenReturn("Pizza_1");
            when(mockProjection1.getDescription()).thenReturn("Description for Pizza_1");
            when(mockProjection1.getImageUrl()).thenReturn("/images/pizza_1");
            when(mockProjection1.getCategory()).thenReturn("Test");
            when(mockProjection1.getStartPrice()).thenReturn(BigDecimal.valueOf(25.0));

            when(mockProjection2.getId()).thenReturn(2L);
            when(mockProjection2.getName()).thenReturn("Pizza_2");
            when(mockProjection2.getDescription()).thenReturn("Description for Pizza_2");
            when(mockProjection2.getImageUrl()).thenReturn("/images/pizza_2");
            when(mockProjection2.getCategory()).thenReturn("Test");
            when(mockProjection2.getStartPrice()).thenReturn(BigDecimal.valueOf(24.0));

            when(mockProjection3.getId()).thenReturn(3L);
            when(mockProjection3.getName()).thenReturn("Pizza_3");
            when(mockProjection3.getDescription()).thenReturn("Description for Pizza_3");
            when(mockProjection3.getImageUrl()).thenReturn("/images/pizza_3");
            when(mockProjection3.getCategory()).thenReturn("Test");
            when(mockProjection3.getStartPrice()).thenReturn(BigDecimal.valueOf(27.0));

            Slice<PizzaCardDto> actual = pizzaService.getPizzaCardsSlice(1, 3);

            assertNotNull(actual);
            assertEquals(actual.getSize(), 3);
            List<PizzaCardDto> cards = actual.getContent();

            assertEquals(cards.get(0).id(), mockProjection1.getId());
            assertEquals(cards.get(0).name(), mockProjection1.getName());
            assertEquals(cards.get(0).description(), mockProjection1.getDescription());
            assertEquals(cards.get(0).imageUrl(), mockProjection1.getImageUrl());
            assertEquals(cards.get(0).category(), mockProjection1.getCategory());
            assertEquals(cards.get(0).startPrice(), mockProjection1.getStartPrice());

            assertEquals(cards.get(1).id(), mockProjection2.getId());
            assertEquals(cards.get(1).name(), mockProjection2.getName());
            assertEquals(cards.get(1).description(), mockProjection2.getDescription());
            assertEquals(cards.get(1).imageUrl(), mockProjection2.getImageUrl());
            assertEquals(cards.get(1).category(), mockProjection2.getCategory());
            assertEquals(cards.get(1).startPrice(), mockProjection2.getStartPrice());

            assertEquals(cards.get(2).id(), mockProjection3.getId());
            assertEquals(cards.get(2).name(), mockProjection3.getName());
            assertEquals(cards.get(2).description(), mockProjection3.getDescription());
            assertEquals(cards.get(2).imageUrl(), mockProjection3.getImageUrl());
            assertEquals(cards.get(2).category(), mockProjection3.getCategory());
            assertEquals(cards.get(2).startPrice(), mockProjection3.getStartPrice());
        }

        @Test
        void shouldReturnEmptySlice_whenNoPizzasFound() {
            Pageable mockPageable = PageRequest.of(0, 3);
            Slice<PizzaRepository.PizzaCardProjection> emptySlice = new SliceImpl<>(
                    Collections.emptyList(),
                    mockPageable,
                    false
            );

            when(pizzaRepository.findAvailablePizzasForCards(mockPageable)).thenReturn(emptySlice);

            Slice<PizzaCardDto> actual = pizzaService.getPizzaCardsSlice(0, 3);

            assertNotNull(actual);
            assertTrue(actual.isEmpty());
            assertEquals(0, actual.getContent().size());
        }

        @Test
        void shouldThrowException_whenInvalidPageNumber() {
            int invalidPage = -1;
            int validSize = 10;

            assertThrows(IllegalArgumentException.class, () -> {
                pizzaService.getPizzaCardsSlice(invalidPage, validSize);
            });
        }

        @Test
        void shouldThrowException_whenInvalidPageSize() {
            int validPage = 0;
            int invalidSize = 0;

            assertThrows(IllegalArgumentException.class, () -> {
                pizzaService.getPizzaCardsSlice(validPage, invalidSize);
            });
        }
    }

    @Nested
    class GetPizzaDetailsTests {

        @Test
        void shouldReturnCorrectPizzaDetails_whenPizzaExists() {
            // Arrange
            Long pizzaId = 1L;
            Pizza mockPizza = Mockito.mock(Pizza.class);
            Set<PizzaIngredient> mockIngredients = createMockIngredients(3);
            Set<PizzaSize> mockSizes = createMockSizes(2);

            when(pizzaRepository.findPizzaDetails(pizzaId)).thenReturn(Optional.of(mockPizza));
//            when(mockPizza.getId()).thenReturn(pizzaId);
            when(mockPizza.getName()).thenReturn("Test Pizza");
            when(mockPizza.getDescription()).thenReturn("Test Description");
            when(mockPizza.getImageUrl()).thenReturn("/images/test");
            when(mockPizza.getCategory()).thenReturn("Test Category");
            when(mockPizza.getCookingTimeMinutes()).thenReturn(20);
            when(mockPizza.getPizzaIngredients()).thenReturn(mockIngredients);
            when(mockPizza.getPizzaSizes()).thenReturn(mockSizes);

            // Act
            PizzaCardDetailsDto result = pizzaService.getPizzaDetails(pizzaId);

            // Assert
            assertNotNull(result);
            assertEquals(pizzaId, result.id());
            assertEquals("Test Pizza", result.name());
            assertEquals("Test Description", result.description());
            assertEquals("/images/test", result.imageUrl());
            assertEquals("Test Category", result.category());
            assertEquals(20, result.cookingTimeMinutes());
            assertEquals(3, result.ingredientInfoDtos().size());
            assertEquals(2, result.pizzaSizeInfoDtos().size());
        }

        private Set<PizzaIngredient> createMockIngredients(int count) {
            Set<PizzaIngredient> ingredients = new HashSet<>();
            for (int i = 0; i < count; i++) {
                PizzaIngredient mockIngredient = Mockito.mock(PizzaIngredient.class);
                Ingredient mockIngredientEntity = Mockito.mock(Ingredient.class);

                when(mockIngredient.getIngredient()).thenReturn(mockIngredientEntity);
                when(mockIngredientEntity.getName()).thenReturn("Ingredient_" + i);

                ingredients.add(mockIngredient);
            }
            return ingredients;
        }

        private Set<PizzaSize> createMockSizes(int count) {
            Set<PizzaSize> sizes = new HashSet<>();
            for (int i = 0; i < count; i++) {
                PizzaSize mockPizzaSize = Mockito.mock(PizzaSize.class);
                SizeTemplate mockSizeTemplate = Mockito.mock(SizeTemplate.class);

                when(mockPizzaSize.getSizeTemplate()).thenReturn(mockSizeTemplate);
                when(mockPizzaSize.getPrice()).thenReturn(BigDecimal.valueOf(20 + i));

                when(mockSizeTemplate.getSizeName()).thenReturn(PizzaSizeEnum.SMALL);
                when(mockSizeTemplate.getDisplayName()).thenReturn("25 см");
                when(mockSizeTemplate.getDiameterCm()).thenReturn(25);
                when(mockSizeTemplate.getWeightGrams()).thenReturn(450);

                sizes.add(mockPizzaSize);
            }
            return sizes;
        }

        @Test
        void shouldThrowEntityNotFoundException_whenPizzaNotFound() {
            // Arrange
            Long nonExistentId = 999L;
            when(pizzaRepository.findPizzaDetails(nonExistentId)).thenReturn(Optional.empty());

            // Act & Assert
            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
                pizzaService.getPizzaDetails(nonExistentId);
            });

            assertEquals("Пицца не найдена в бд", exception.getMessage());
        }

        @Test
        void shouldThrowException_whenIdIsNull() {
            // Act & Assert
            assertThrows(EntityNotFoundException.class, () -> {
                pizzaService.getPizzaDetails(null);
            });
        }

        @Test
        void shouldMapIngredientInfoCorrectly() {
            // Arrange
            Long pizzaId = 1L;
            Pizza mockPizza = Mockito.mock(Pizza.class);
            PizzaIngredient mockIngredient = Mockito.mock(PizzaIngredient.class);
            Ingredient mockIngredientEntity = Mockito.mock(Ingredient.class);

            when(pizzaRepository.findPizzaDetails(pizzaId)).thenReturn(Optional.of(mockPizza));
            //when(mockPizza.getId()).thenReturn(pizzaId);
            when(mockPizza.getName()).thenReturn("Test Pizza");
            when(mockPizza.getDescription()).thenReturn("Description");
            when(mockPizza.getImageUrl()).thenReturn("/images/test");
            when(mockPizza.getCategory()).thenReturn("Test");
            when(mockPizza.getCookingTimeMinutes()).thenReturn(20);
            when(mockPizza.getPizzaIngredients()).thenReturn(Set.of(mockIngredient));
            when(mockPizza.getPizzaSizes()).thenReturn(Collections.emptySet());

            when(mockIngredient.getIngredient()).thenReturn(mockIngredientEntity);
            when(mockIngredientEntity.getName()).thenReturn("Test Ingredient");

            // Act
            PizzaCardDetailsDto result = pizzaService.getPizzaDetails(pizzaId);

            // Assert
            assertEquals(1, result.ingredientInfoDtos().size());
            assertEquals("Test Ingredient", result.ingredientInfoDtos().get(0).name());
        }

        @Test
        void shouldMapSizeInfoCorrectly() {
            // Arrange
            Long pizzaId = 1L;
            Pizza mockPizza = Mockito.mock(Pizza.class);
            PizzaSize mockPizzaSize = Mockito.mock(PizzaSize.class);
            SizeTemplate mockSizeTemplate = Mockito.mock(SizeTemplate.class);

            when(pizzaRepository.findPizzaDetails(pizzaId)).thenReturn(Optional.of(mockPizza));

            when(mockPizza.getDescription()).thenReturn("Description");
            when(mockPizza.getImageUrl()).thenReturn("/images/test");
            when(mockPizza.getCategory()).thenReturn("Test");
            when(mockPizza.getCookingTimeMinutes()).thenReturn(20);
            when(mockPizza.getPizzaIngredients()).thenReturn(Collections.emptySet());
            when(mockPizza.getPizzaSizes()).thenReturn(Set.of(mockPizzaSize));

            when(mockPizzaSize.getSizeTemplate()).thenReturn(mockSizeTemplate);
            when(mockPizzaSize.getPrice()).thenReturn(BigDecimal.valueOf(25.0));

            when(mockSizeTemplate.getSizeName()).thenReturn(PizzaSizeEnum.MEDIUM);
            when(mockSizeTemplate.getDisplayName()).thenReturn("Средняя");
            when(mockSizeTemplate.getDiameterCm()).thenReturn(30);
            when(mockSizeTemplate.getWeightGrams()).thenReturn(500);

            // Act
            PizzaCardDetailsDto result = pizzaService.getPizzaDetails(pizzaId);

            // Assert
            assertEquals(1, result.pizzaSizeInfoDtos().size());
            PizzaSizeInfoDto sizeInfo = result.pizzaSizeInfoDtos().get(0);
            assertEquals(BigDecimal.valueOf(25.0), sizeInfo.price());

            SizeTemplateInfoDto templateInfo = sizeInfo.sizeTemplateInfoDto();
            assertEquals(PizzaSizeEnum.MEDIUM, templateInfo.sizeName());
            assertEquals("Средняя", templateInfo.displayName());
            assertEquals(30, templateInfo.diameterCm());
            assertEquals(500, templateInfo.weightGrams());
        }
    }

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
                    15,
                    Map.of(1L, 200),
                    Set.of(1L)
            );

            Ingredient mockIngredient = Mockito.mock(Ingredient.class);
            when(mockIngredient.getId()).thenReturn(1L);
            when(mockIngredient.getName()).thenReturn("Моцарелла");

            SizeTemplate mockSizeTemplate = Mockito.mock(SizeTemplate.class);

            Pizza mockPizza = Mockito.mock(Pizza.class);
            when(mockPizza.getPizzaIngredients()).thenReturn(Collections.emptySet());
            when(mockPizza.getPizzaSizes()).thenReturn(Collections.emptySet());

            PizzaReadDto expectedDto = new PizzaReadDto(
                    1L, "Маргарита", "Классическая пицца",
                    "/images/margherita.jpg", "Классические", true, 15,
                    LocalDateTime.now(),
                    List.of(), List.of()
            );

            when(pizzaRepository.existsByName("Маргарита")).thenReturn(false);
            when(ingredientRepository.findAllById(Set.of(1L))).thenReturn(List.of(mockIngredient));
            when(sizeTemplateRepository.findAllById(Set.of(1L))).thenReturn(List.of(mockSizeTemplate));

            when(pizzaCreateMapper.map(dto)).thenReturn(mockPizza);
            when(pizzaReadMapper.map(mockPizza)).thenReturn(expectedDto);

            when(priceCalculator.calculatePrice(
                    any(SizeTemplate.class),
                    anyMap(),
                    anyMap()
            )).thenReturn(new BigDecimal("15.90"));

            PizzaReadDto actualDto = pizzaService.createPizza(dto);

            assertNotNull(actualDto);
            assertEquals("Маргарита", actualDto.name());

            verify(pizzaRepository, times(1)).save(mockPizza);
        }

        @Test
        void createPizzaWithoutIngredientsShouldThrow() {
            PizzaCreateDto dto = new PizzaCreateDto(
                    "Маргарита",
                    "Классическая пицца",
                    "/images/margherita.jpg",
                    "Классические",
                    true,
                    15,
                    Collections.emptyMap(),
                    Set.of(1L)
            );

            when(pizzaRepository.existsByName("Маргарита")).thenReturn(false);

            assertThatThrownBy(() -> pizzaService.createPizza(dto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Пицца должна содержать ингредиенты");

            verify(ingredientRepository, never()).findAllById(anyIterable());
            verify(sizeTemplateRepository, never()).findAllById(anyIterable());
            verify(pizzaCreateMapper, never()).map(any());
            verify(pizzaRepository, never()).save(any());
            verify(pizzaReadMapper, never()).map(any());

        }

        @Test
        void createPizzaShouldThrow_ifNameAlreadyExists() {
            PizzaCreateDto dto = new PizzaCreateDto(
                    "Маргарита",
                    "Классическая пицца",
                    "/images/margherita.jpg",
                    "Классические",
                    true,
                    15,
                    Collections.emptyMap(),
                    Set.of(1L)
            );

            when(pizzaRepository.existsByName("Маргарита")).thenReturn(true);

            assertThatThrownBy(() -> pizzaService.createPizza(dto))
                    .isInstanceOf(PizzaAlreadyExistsException.class)
                    .hasMessageContaining("Пицца с таким именем уже существует");

            verify(ingredientRepository, never()).findAllById(anyIterable());
            verify(sizeTemplateRepository, never()).findAllById(anyIterable());
            verify(pizzaCreateMapper, never()).map(any());
            verify(pizzaRepository, never()).save(any());
            verify(pizzaReadMapper, never()).map(any());

        }
    }

    @Nested
    class UpdatePizzaTests {
        @Test
        void shouldUpdate_ifDataIsCorrect() {
            Long idToUpdate = 1L;

            PizzaUpdateDto updateDto = new PizzaUpdateDto(
                    "Маргарита-Про-Макс",
                    "Это не просто Маргарита, а Маргарита про макс",
                    "/images/margherita-pro-max.jpg",
                    "ПИЦЦЫ ПРО МАКС",
                    true,
                    20
            );

            Pizza mockPizzaToUpdate = Mockito.mock(Pizza.class);

            when(pizzaRepository.findById(idToUpdate)).thenReturn(Optional.of(mockPizzaToUpdate));

            when(pizzaUpdateMapper.map(updateDto, mockPizzaToUpdate)).thenReturn(mockPizzaToUpdate);

            when(pizzaRepository.save(mockPizzaToUpdate)).thenReturn(mockPizzaToUpdate);

            PizzaReadDto expectedReadDto = new PizzaReadDto(
                    idToUpdate,
                    updateDto.name(),
                    updateDto.description(),
                    updateDto.imageUrl(),
                    updateDto.category(),
                    updateDto.available(),
                    updateDto.cookingTimeMinutes(),
                    LocalDateTime.now(),
                    List.of(),
                    List.of()
            );

            when(pizzaReadMapper.map(mockPizzaToUpdate)).thenReturn(expectedReadDto);

            PizzaUpdateResponseDto actualResult = pizzaService.updatePizzaById(idToUpdate, updateDto);

            assertEquals(actualResult.name(), expectedReadDto.name());
            assertEquals(actualResult.category(), expectedReadDto.category());
            assertEquals(actualResult.description(), expectedReadDto.description());
            assertEquals(actualResult.cookingTimeMinutes(), expectedReadDto.cookingTimeMinutes());

            verify(pizzaRepository).save(mockPizzaToUpdate);
            verify(pizzaRepository).findById(idToUpdate);
            verify(pizzaUpdateMapper).map(updateDto, mockPizzaToUpdate);
            verify(pizzaReadMapper).map(mockPizzaToUpdate);
        }

        @Test
        void shouldUpdateOnlyNotNullFieldsFromDto_whenDataIsCorrect() {
            Long idToUpdate = 1L;

            PizzaUpdateDto updateDto = new PizzaUpdateDto(
                    "Маргарита-Про-Макс",
                    "Это не просто Маргарита, а Маргарита про макс",
                    null,
                    null,
                    null,
                    null
            );

            String nameBefore = "Маргарита";
            String descriptionBefore = "Классическая пицца";

            Pizza PizzaToUpdate = Pizza.create(
                    nameBefore,
                    descriptionBefore,
                    "/images/margherita.jpg",
                    "Классические",
                    true,
                    12
            );

            when(pizzaRepository.findById(idToUpdate)).thenReturn(Optional.of(PizzaToUpdate));

            when(pizzaUpdateMapper.map(updateDto, PizzaToUpdate)).thenReturn(PizzaToUpdate);

            when(pizzaRepository.save(PizzaToUpdate)).thenReturn(PizzaToUpdate);

            PizzaReadDto expectedReadDto = new PizzaReadDto(
                    idToUpdate,
                    updateDto.name(),
                    updateDto.description(),
                    PizzaToUpdate.getImageUrl(),
                    PizzaToUpdate.getCategory(),
                    PizzaToUpdate.isAvailable(),
                    PizzaToUpdate.getCookingTimeMinutes(),
                    LocalDateTime.now(),
                    List.of(),
                    List.of()
            );

            when(pizzaReadMapper.map(PizzaToUpdate)).thenReturn(expectedReadDto);

            PizzaUpdateResponseDto actualResult = pizzaService.updatePizzaById(idToUpdate, updateDto);

            assertEquals(idToUpdate, actualResult.id());
            assertEquals(actualResult.name(), expectedReadDto.name());
            assertEquals(actualResult.description(), expectedReadDto.description());
            assertEquals(actualResult.category(), expectedReadDto.category());
            assertEquals(actualResult.cookingTimeMinutes(), expectedReadDto.cookingTimeMinutes());

            assertEquals(PizzaToUpdate.getImageUrl(), actualResult.imageUrl());
            assertEquals(PizzaToUpdate.getCategory(), actualResult.category());
            assertEquals(PizzaToUpdate.isAvailable(), actualResult.isAvailable());
            assertEquals(PizzaToUpdate.getCookingTimeMinutes(), actualResult.cookingTimeMinutes());

            assertNotEquals(nameBefore, actualResult.name());
            assertNotEquals(descriptionBefore, actualResult.description());

            verify(pizzaRepository).findById(idToUpdate);
            verify(pizzaUpdateMapper).map(updateDto, PizzaToUpdate);
            verify(pizzaRepository).save(PizzaToUpdate);
            verify(pizzaReadMapper).map(PizzaToUpdate);
        }

        @Test
        void shouldThrow_whenPizzaToUpdateNotFound() {
            Long idToFail = 99999L;

            when(pizzaRepository.findById(idToFail)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> pizzaService.updatePizzaById(idToFail, mock(PizzaUpdateDto.class)))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Пицца для обновления с id 99999 не найдена в БД");

            verify(pizzaUpdateMapper, never()).map(any());
            verify(pizzaRepository, never()).save(any());
            verify(pizzaReadMapper, never()).map(any());
        }


    }

    @Nested
    class DeletePizzaTests {

        @Test
        void deletePizzaShouldDelete_whenCorrectData() {
            Long idToDelete = 1L;

            Pizza mockPizzaToDelete = Mockito.mock(Pizza.class);
            when(mockPizzaToDelete.getId()).thenReturn(idToDelete);
            when(mockPizzaToDelete.getName()).thenReturn("Маргарита");

            when(pizzaRepository.findById(idToDelete)).thenReturn(Optional.of(mockPizzaToDelete));

            assertThatCode(() -> pizzaService.deletePizzaById(idToDelete))
                    .doesNotThrowAnyException();
        }
    }
}