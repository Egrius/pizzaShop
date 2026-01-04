package by.egrius.pizzaShop.service;

import by.egrius.pizzaShop.dto.user.ChangeFullNameDto;
import by.egrius.pizzaShop.dto.user.ChangePasswordDto;
import by.egrius.pizzaShop.dto.user.UserCreateDto;
import by.egrius.pizzaShop.dto.user.UserReadDto;
import by.egrius.pizzaShop.entity.User;
import by.egrius.pizzaShop.exception.InvalidPasswordException;
import by.egrius.pizzaShop.exception.UserAlreadyExistsException;
import by.egrius.pizzaShop.exception.UserNotFoundException;
import by.egrius.pizzaShop.mapper.user.UserReadMapper;
import by.egrius.pizzaShop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserReadMapper userReadMapper;
    private final PasswordEncoder passwordEncoder;

    private String buildChangeLog(boolean nameChanged, User user) {
        List<String> changes = new ArrayList<>();
        if (nameChanged) changes.add("Имя: " + user.getFullName());
        return String.join(", ", changes);
    }

    public UserReadDto getUserById(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
        return userReadMapper.map(user);
    }

    @Transactional
    public UserReadDto createUser(UserCreateDto userCreateDto) {

        if (userRepository.existsByEmail(userCreateDto.email())) {
            throw new UserAlreadyExistsException(
                    "Пользователь с email " + userCreateDto.email() + " уже существует"
            );
        }

        if (userRepository.existsByPhone(userCreateDto.phone())) {
            throw new UserAlreadyExistsException(
                    "Пользователь с телефоном " + userCreateDto.phone() + " уже существует"
            );
        }

        String encodedPassword = passwordEncoder.encode(userCreateDto.rawPassword());

        User newUser = User.createUser(
                userCreateDto.fullName(),
                userCreateDto.email(),
                userCreateDto.phone(),
                encodedPassword
        );

        User savedUser = userRepository.save(newUser);
        log.info("Создан пользователь с ID: {} , Имя: {}",
                savedUser.getId(), savedUser.getFullName());

        return userReadMapper.map(savedUser);
    }

    @Transactional
    public UserReadDto changeFullName(Long userId, ChangeFullNameDto updateDto) {

        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        boolean nameChanged = false;

        if (!updateDto.newFullName().isEmpty() &&
                !updateDto.newFullName().equals(user.getFullName())) {
            user.changeFullName(updateDto.newFullName());
            nameChanged = true;
        }

        if (nameChanged) {
            log.info("Обновлён пользователь с ID: {} - {}",
                    user.getId(),
                    buildChangeLog(nameChanged, user));
        }

        return userReadMapper.map(user);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordDto changePasswordDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        if (!passwordEncoder.matches(changePasswordDto.currentPassword(), user.getPasswordHash())) {
            throw new InvalidPasswordException("Введённый текущий пароль неверен");
        }

        user.changePassword(changePasswordDto.newPassword(), passwordEncoder);

        log.info("Пароль пользователя с ID: {} изменён", userId);
    }

    @Transactional
    public void deleteUser(Long userId, String rawPassword) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new InvalidPasswordException("Пароль введён неправильно");
        }

        userRepository.delete(user);

        log.info("Удалён пользователь с ID: {} , Имя: {}",
                user.getId(), user.getFullName());
    }
}