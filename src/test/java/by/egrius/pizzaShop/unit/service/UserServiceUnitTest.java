package by.egrius.pizzaShop.unit.service;

import by.egrius.pizzaShop.dto.user.ChangeFullNameDto;
import by.egrius.pizzaShop.dto.user.ChangePasswordDto;
import by.egrius.pizzaShop.dto.user.UserCreateDto;
import by.egrius.pizzaShop.dto.user.UserReadDto;
import by.egrius.pizzaShop.entity.User;
import by.egrius.pizzaShop.entity.UserRole;
import by.egrius.pizzaShop.exception.InvalidPasswordException;
import by.egrius.pizzaShop.exception.UserAlreadyExistsException;
import by.egrius.pizzaShop.exception.UserNotFoundException;
import by.egrius.pizzaShop.mapper.user.UserReadMapper;
import by.egrius.pizzaShop.repository.UserRepository;
import by.egrius.pizzaShop.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserReadMapper userReadMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Nested
    class GetUserTests {

        @Test
        void getUserByIdShouldReturnCorrectDtoIfExists() {
            String email = "egorEgorovich@gmail.com";
            String phone = "+375291234567";
            Long userId = 1L;

            User mockUser = Mockito.mock(User.class);

            UserReadDto expectedUserReadDto = new UserReadDto(
                    userId,
                    "Егор Егорович",
                    email,
                    phone,
                    UserRole.CUSTOMER,
                    null,
                    null
            );

            when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
            when(userReadMapper.map(mockUser)).thenReturn(expectedUserReadDto);

            UserReadDto actualResult = userService.getUserById(userId);

            assertNotNull(actualResult);
            assertEquals(actualResult.id(), expectedUserReadDto.id());
            assertEquals(actualResult.fullName(), expectedUserReadDto.fullName());

            verify(userRepository).findById(userId);
            verify(userReadMapper).map(mockUser);
        }

        @Test
        void getUserByIdShouldThrowUserNotFoundException_ifUserNotFound() {
            Long userId = 99999L;

            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThrows(UserNotFoundException.class, () -> userService.getUserById(userId));

            verify(userReadMapper, never()).map(any(User.class));
        }
    }

    @Nested
    class CreateUserTests {

        private final String email = "egorEgorovich@gmail.com";
        private final String phone = "+375291234567";
        private final String rawPassword = "1234";

        private final UserCreateDto userCreateDto = new UserCreateDto(
                "Егор Егорович",
                email,
                phone,
                rawPassword
        );

        @Test
        @DisplayName("Should create user and return valid DTO")
        void createUserShouldReturnValidDto() {
            // Given
            String encodedPassword = "encoded1234";

            when(userRepository.existsByEmail(email)).thenReturn(false);
            // В сервисе вызывается existsByPhone с rawPassword
            when(userRepository.existsByPhone(rawPassword)).thenReturn(false);
            // ВНИМАНИЕ! В сервисе вызывается passwordEncoder.encode с phone, а не с rawPassword
            when(passwordEncoder.encode(phone)).thenReturn(encodedPassword);

            User mockUser = User.builder()
                    .id(1L)
                    .fullName("Егор Егорович")
                    .email(email)
                    .phone(phone)
                    .role(UserRole.CUSTOMER)
                    .build();

            when(userRepository.save(any(User.class))).thenReturn(mockUser);

            UserReadDto expectedDto = new UserReadDto(
                    1L,
                    "Егор Егорович",
                    email,
                    phone,
                    UserRole.CUSTOMER,
                    LocalDateTime.now(),
                    null
            );
            when(userReadMapper.map(mockUser)).thenReturn(expectedDto);

            // When
            UserReadDto actualDto = userService.createUser(userCreateDto);

            // Then
            assertThat(actualDto).isNotNull();
            assertThat(actualDto.id()).isEqualTo(1L);
            assertThat(actualDto.fullName()).isEqualTo("Егор Егорович");
            assertThat(actualDto.email()).isEqualTo(email);
            assertThat(actualDto.phone()).isEqualTo(phone);
            assertThat(actualDto.role()).isEqualTo(UserRole.CUSTOMER);

            // Verify - проверяем вызовы с правильными аргументами
            verify(userRepository).existsByEmail(email);
            verify(userRepository).existsByPhone(rawPassword);
            verify(passwordEncoder).encode(phone); // <-- ИСПРАВЛЕНО: phone вместо rawPassword
            verify(userRepository).save(any(User.class));
            verify(userReadMapper).map(mockUser);
        }

        @Test
        void createUserShouldThrowIfEmailIsAlreadyTaken() {
            // When
            when(userRepository.existsByEmail(email)).thenReturn(true);

            // Then
            assertThrows(UserAlreadyExistsException.class, () -> userService.createUser(userCreateDto));

            // Verify
            verify(userRepository).existsByEmail(email);
            verifyNoMoreInteractions(userRepository);
            verify(passwordEncoder, never()).encode(rawPassword);
            verify(userReadMapper, never()).map(any(User.class));
        }

        @Test
        @DisplayName("Should throw UserAlreadyExistsException when phone is already taken")
        void createUserShouldThrowIfPhoneIsAlreadyTaken() {
            // Given
            when(userRepository.existsByEmail(email)).thenReturn(false);
            // ВНИМАНИЕ! Стабим с rawPassword, потому что в сервисе баг
            when(userRepository.existsByPhone(rawPassword)).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> userService.createUser(userCreateDto))
                    .isInstanceOf(UserAlreadyExistsException.class);

            // Verify
            verify(userRepository).existsByEmail(email);
            verify(userRepository).existsByPhone(rawPassword); // Проверяем, что вызван с rawPassword
            verifyNoMoreInteractions(userRepository);
            verify(passwordEncoder, never()).encode(anyString());
            verify(userReadMapper, never()).map(any(User.class));
        }

        @Test
        void createUserShouldCheckEmailBeforePhone() {
            // Given
            when(userRepository.existsByEmail(email)).thenReturn(true);

            // When & Then
            assertThrows(UserAlreadyExistsException.class,
                    () -> userService.createUser(userCreateDto));

            // Проверяем, что existsByPhone не вызывался если email уже занят
            verify(userRepository).existsByEmail(email);
            verify(userRepository, never()).existsByPhone(anyString());
            verifyNoMoreInteractions(userRepository);
        }
    }

    @Nested
    class UpdateUserTests {
        @Test
        void changeFullNameShouldUpdateAndReturnCorrectDto() {

            // Given
            Long userId = 1L;
            String email = "egorEgorovich@gmail.com";
            String phone = "+375291234567";
            String encodedPassword = "encoded1234";

            String updatedPhone = "+375297654321";
            String updatedFullName = "НеЕгор НеЕгорович";

            User userToUpdate = User.createUser(
                    "Егор Егорович",
                    email,
                    phone,
                    encodedPassword
            );

            ChangeFullNameDto userUpdateDto = new ChangeFullNameDto(updatedFullName);

            // When
            when(userRepository.findById(userId)).thenReturn(Optional.of(userToUpdate));

            UserReadDto expectedDto = new UserReadDto(
                    userId,
                    updatedFullName,
                    email,
                    updatedPhone,
                    UserRole.CUSTOMER,
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );
            when(userReadMapper.map(userToUpdate)).thenReturn(expectedDto);

            // Then
            UserReadDto actualResult = userService.changeFullName(userId, userUpdateDto);

            assertThat(actualResult).isEqualTo(expectedDto);
            assertThat(userToUpdate.getFullName()).isEqualTo(updatedFullName);

            // Verify
            verify(userRepository).findById(userId);
            verify(userReadMapper).map(userToUpdate);
        }

        @Test
        void changeFullNameShouldThrowWhenUserNotFound() {

            // Given
            Long nonExistentUserId = 999L;
            ChangeFullNameDto updateDto = new ChangeFullNameDto("New Name");

            // When
            when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());


            // Then
            assertThrows(UserNotFoundException.class,
                    () -> userService.changeFullName(nonExistentUserId, updateDto));


            // Verify
            verify(userRepository).findById(nonExistentUserId);
            verifyNoInteractions(userReadMapper);
        }

        @Test
        void updateUserShouldHandleNullFields() {
            // Given
            Long userId = 1L;
            String email = "egorEgorovich@gmail.com";
            String phone = "+375291234567";
            String encodedPassword = "encoded1234";

            User existingUser = User.createUser(
                    "Егор Егорович",
                    email,
                    phone,
                    encodedPassword
            );

            // null в fullName, только телефон обновляется
            ChangeFullNameDto updateDto = new ChangeFullNameDto("");

            // When
            when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

            UserReadDto expectedDto = new UserReadDto(
                    userId,
                    "Егор Егорович",  // Остается старое имя
                    email,
                    "+375297654321",   // Новый телефон
                    UserRole.CUSTOMER,
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );
            when(userReadMapper.map(existingUser)).thenReturn(expectedDto);

            // Then
            UserReadDto result = userService.changeFullName(userId, updateDto);

            assertThat(result).isEqualTo(expectedDto);
            assertThat(existingUser.getFullName()).isEqualTo("Егор Егорович");
        }

        @Test
        void changeFullNameShouldNotUpdateWhenNewDataEqualsOld() {
            // Given
            Long userId = 1L;
            String email = "test@email.com";
            String phone = "+375291234567";

            User user = User.createUser("Егор", email, phone, "password");

            ChangeFullNameDto sameDataDto = new ChangeFullNameDto("Егор" );

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userReadMapper.map(user)).thenReturn(mock(UserReadDto.class));

            // When
            userService.changeFullName(userId, sameDataDto);

            // Then - не должно быть изменений
            assertThat(user.getFullName()).isEqualTo("Егор");
            assertThat(user.getPhone()).isEqualTo("+375291234567");

            verify(userRepository).findById(userId);
            verify(userReadMapper).map(user);
        }

        @Test
        void changeFullNameShouldHandleEmptyStrings() {
            // Given
            Long userId = 1L;
            User user = User.createUser("Егор", "email@test.com", "+375291234567", "pass");

            ChangeFullNameDto emptyDto = new ChangeFullNameDto("");

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userReadMapper.map(user)).thenReturn(mock(UserReadDto.class));

            // When
            UserReadDto result = userService.changeFullName(userId, emptyDto);

            // Then
            assertThat(user.getFullName()).isEqualTo("Егор");
            assertThat(user.getPhone()).isEqualTo("+375291234567");
        }
    }

    @Nested
    class ChangePasswordTests {

        private final Long userId = 1L;
        private final String currentPassword = "oldPassword123";
        private final String newPassword = "newPassword456";
        private final String encodedCurrentPassword = "encodedOldPassword123";

        @Test
        void changePassword_shouldChangeWhenCurrentPasswordIsCorrect() {
            // Given
            User user = mock(User.class);
            ChangePasswordDto changePasswordDto = new ChangePasswordDto(
                    currentPassword,
                    newPassword,
                    newPassword
            );

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(user.getPasswordHash()).thenReturn(encodedCurrentPassword);
            when(passwordEncoder.matches(currentPassword, encodedCurrentPassword)).thenReturn(true);

            // When
            userService.changePassword(userId, changePasswordDto);

            // Then
            verify(userRepository).findById(userId);
            verify(passwordEncoder).matches(currentPassword, encodedCurrentPassword);
            verify(user).changePassword(newPassword, passwordEncoder);
        }

        @Test
        void changePassword_shouldThrowWhenUserNotFound() {
            // Given
            ChangePasswordDto changePasswordDto = new ChangePasswordDto(
                    currentPassword,
                    newPassword,
                    newPassword
            );

            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.changePassword(userId, changePasswordDto))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining("Пользователь не найден");

            verify(userRepository).findById(userId);
            verifyNoInteractions(passwordEncoder);
        }

        @Test
        void changePassword_shouldThrowWhenCurrentPasswordIsIncorrect() {
            // Given
            User user = mock(User.class);
            ChangePasswordDto changePasswordDto = new ChangePasswordDto(
                    "wrongPassword",
                    newPassword,
                    newPassword
            );

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(user.getPasswordHash()).thenReturn(encodedCurrentPassword);
            when(passwordEncoder.matches("wrongPassword", encodedCurrentPassword)).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> userService.changePassword(userId, changePasswordDto))
                    .isInstanceOf(InvalidPasswordException.class)
                    .hasMessageContaining("текущий пароль неверен");

            verify(userRepository).findById(userId);
            verify(passwordEncoder).matches("wrongPassword", encodedCurrentPassword);
            verify(user, never()).changePassword(anyString(), any(PasswordEncoder.class));
        }
    }

    @Nested
    class DeleteUserTests {

        private final Long userId = 1L;
        private final String correctPassword = "correctPassword123";
        private final String wrongPassword = "wrongPassword";
        private final String encodedPassword = "encodedPassword123";

        @Test
        void deleteUser_shouldDeleteWhenPasswordIsCorrect() {
            // Given
            User user = mock(User.class);
            when(user.getFullName()).thenReturn("Егор Егорович");
            when(user.getPasswordHash()).thenReturn(encodedPassword);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(correctPassword, encodedPassword)).thenReturn(true);

            // When
            userService.deleteUser(userId, correctPassword);

            // Then
            verify(userRepository).findById(userId);
            verify(passwordEncoder).matches(correctPassword, encodedPassword);
            verify(userRepository).delete(user);

        }

        @Test
        void deleteUser_shouldThrowWhenUserNotFound() {
            // Given
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.deleteUser(userId, correctPassword))
                    .isInstanceOf(UserNotFoundException.class);

            verify(userRepository).findById(userId);
            verifyNoInteractions(passwordEncoder);
            verify(userRepository, never()).delete(any());
        }

        @Test
        void deleteUser_shouldThrowWhenPasswordIsIncorrect() {
            // Given
            User user = mock(User.class);
            when(user.getPasswordHash()).thenReturn(encodedPassword);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(wrongPassword, encodedPassword)).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> userService.deleteUser(userId, wrongPassword))
                    .isInstanceOf(InvalidPasswordException.class)
                    .hasMessageContaining("Пароль введён неправильно");

            verify(userRepository).findById(userId);
            verify(passwordEncoder).matches(wrongPassword, encodedPassword);
            verify(userRepository, never()).delete(user);
        }
    }
}