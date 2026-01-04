package by.egrius.pizzaShop.integration.service;

import by.egrius.pizzaShop.dto.user.ChangeFullNameDto;
import by.egrius.pizzaShop.dto.user.ChangePasswordDto;
import by.egrius.pizzaShop.dto.user.UserCreateDto;
import by.egrius.pizzaShop.dto.user.UserReadDto;
import by.egrius.pizzaShop.entity.User;
import by.egrius.pizzaShop.entity.UserRole;
import by.egrius.pizzaShop.exception.InvalidPasswordException;
import by.egrius.pizzaShop.exception.UserAlreadyExistsException;
import by.egrius.pizzaShop.exception.UserNotFoundException;
import by.egrius.pizzaShop.repository.UserRepository;
import by.egrius.pizzaShop.service.UserService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceIT {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Nested
    class CreateUserTests {
        @Test
        void shouldCreateUserSuccessfully() {
            // Given
            UserCreateDto createDto = new UserCreateDto(
                    "Иван Иванов",
                    "ivan@test.com",
                    "+375291234567",
                    "password123"
            );

            // When
            UserReadDto result = userService.createUser(createDto);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.id()).isNotNull();
            assertThat(result.email()).isEqualTo("ivan@test.com");
            assertThat(result.fullName()).isEqualTo("Иван Иванов");
            assertThat(result.phone()).isEqualTo("+375291234567");
            assertThat(result.role()).isEqualTo(UserRole.CUSTOMER);

            // Проверяем что сохранилось в БД
            Optional<User> savedUser = userRepository.findByEmail("ivan@test.com");
            assertThat(savedUser).isPresent();
            assertThat(savedUser.get().getFullName()).isEqualTo("Иван Иванов");
        }

        @Test
        void shouldThrow_ifCreateWithExistingData() {
            UserCreateDto createDto = new UserCreateDto(
                    "Иван Иванов",
                    "ivan@test.com",
                    "+375291234567",
                    "password123"
            );

            // When
            UserReadDto result = userService.createUser(createDto);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.id()).isNotNull();
            assertThat(result.email()).isEqualTo("ivan@test.com");
            assertThat(result.fullName()).isEqualTo("Иван Иванов");
            assertThat(result.phone()).isEqualTo("+375291234567");
            assertThat(result.role()).isEqualTo(UserRole.CUSTOMER);

            UserCreateDto createDtoToFail = new UserCreateDto(
                    "Иван Иванов",
                    "ivan_2@test.com",
                    "+375291234567",
                    "password123"
            );

            assertThatThrownBy(() -> userService.createUser(createDtoToFail))
                    .isInstanceOf(UserAlreadyExistsException.class)
                    .hasMessageContaining("телефоном");

            assertThat(userRepository.count()).isEqualTo(1);
        }

        @Test
        void shouldThrowWhenCreatingUserWithExistingEmail() {
            UserCreateDto createDto = new UserCreateDto(
                    "Иван Иванов",
                    "ivan@test.com",
                    "+375291234567",
                    "password123"
            );

            // When
            UserReadDto result = userService.createUser(createDto);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.id()).isNotNull();
            assertThat(result.email()).isEqualTo("ivan@test.com");
            assertThat(result.fullName()).isEqualTo("Иван Иванов");
            assertThat(result.phone()).isEqualTo("+375291234567");
            assertThat(result.role()).isEqualTo(UserRole.CUSTOMER);

            UserCreateDto createDtoToFail = new UserCreateDto(
                    "Иван Иванов",
                    "ivan@test.com",
                    "+375291234560",
                    "password123"
            );

            assertThatThrownBy(() -> userService.createUser(createDtoToFail))
                    .isInstanceOf(UserAlreadyExistsException.class)
                    .hasMessageContaining("email");

            assertThat(userRepository.count()).isEqualTo(1);
        }
    }

    @Nested
    class UpdateUserTests {

        private User userToTest;

        private Long userToTestId;

        @BeforeEach
        void setup() {
            UserCreateDto createDto = new UserCreateDto(
                    "Иван Иванов",
                    "ivan@test.com",
                    "+375291234567",
                    "password123"
            );

            UserReadDto result = userService.createUser(createDto);

            userToTestId = result.id();

            userToTest = userRepository.findById(result.id()).orElseThrow(RuntimeException::new);
        }

        @AfterEach
        void cleanup() {
            userRepository.delete(userToTest);
            userToTestId = null;
        }

        @Test
        void updateUserShouldUpdate_ifCorrectData() {
            ChangeFullNameDto correctUpdateDto = new ChangeFullNameDto(
                    "Новый Иван"
            );

            userService.changeFullName(userToTestId, correctUpdateDto);

            assertThat(userToTest.getFullName()).isEqualTo("Новый Иван");
        }

        @Test
        void updateUserShouldThrow_ifUserNotFound() {

            ChangeFullNameDto correctUpdateDto = new ChangeFullNameDto(
                    "Новый Иван"
            );

            assertThatThrownBy(() ->  userService.changeFullName(99999L, correctUpdateDto))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining("не найден");

        }

        @Test
        void shouldChangePasswordSuccessfully() {
            // Given
            ChangePasswordDto dto = new ChangePasswordDto(
                    "password123",  // текущий правильный
                    "newPassword456",  // новый
                    "newPassword456"   // подтверждение
            );

            // When
            assertThatCode(() -> userService.changePassword(userToTestId, dto))
                    .doesNotThrowAnyException();

            // Then - проверяем что новый пароль работает
            User user = userRepository.findById(userToTestId).orElseThrow();
            assertThat(passwordEncoder.matches("newPassword456", user.getPasswordHash()))
                    .isTrue();
        }

        @Test
        void shouldThrowWhenCurrentPasswordIncorrect() {
            ChangePasswordDto dto = new ChangePasswordDto(
                    "wrongPassword",   // неверный текущий
                    "newPassword456",
                    "newPassword456"
            );

            assertThatThrownBy(() -> userService.changePassword(userToTestId, dto))
                    .isInstanceOf(InvalidPasswordException.class)
                    .hasMessageContaining("текущий пароль неверен");
        }

        @Test
        void shouldThrow_whenConfirmedPasswordIsIncorrect() {
            assertThatThrownBy(() -> new ChangePasswordDto(
                    "password123",
                    "newPassword456",
                    "newPassword4569"
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Пароли не совпадают");
        }

    }

    @Nested
    class DeleteUserTests {

        private User userToTest;

        private Long userToTestId;

        @BeforeEach
        void setup() {
            UserCreateDto createDto = new UserCreateDto(
                    "Иван Иванов",
                    "ivan@test.com",
                    "+375291234567",
                    "password123"
            );

            UserReadDto result = userService.createUser(createDto);

            userToTestId = result.id();

            userToTest = userRepository.findById(result.id()).orElseThrow(RuntimeException::new);
        }

        @AfterEach
        void cleanup() {
            userRepository.delete(userToTest);
            userToTestId = null;
        }

        @Test
        void shouldDeleteIfUserFoundAndCorrectPassword() {
            assertThatCode(() -> userService.deleteUser(userToTestId, "password123"))
                    .doesNotThrowAnyException();

            assertThat(userRepository.findById(userToTestId)).isEmpty();
        }

        @Test
        void shouldThrowUserNotFoundException_whenUserDoesNotExist() {
            Long nonExistentId = 999999L;

            assertThatThrownBy(() -> userService.deleteUser(nonExistentId, "password123"))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining("Пользователь не найден");
        }

        @Test
        void shouldThrowInvalidPasswordException_whenPasswordIsIncorrect() {
            assertThatThrownBy(() -> userService.deleteUser(userToTestId, "wrongPassword"))
                    .isInstanceOf(InvalidPasswordException.class)
                    .hasMessageContaining("Пароль введён неправильно");
        }
    }
}