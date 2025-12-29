package by.egrius.pizzaShop.service;

import by.egrius.pizzaShop.dto.UserCreateDto;
import by.egrius.pizzaShop.dto.UserReadDto;
import by.egrius.pizzaShop.entity.User;
import by.egrius.pizzaShop.exception.UserAlreadyExistsException;
import by.egrius.pizzaShop.mapper.UserReadMapper;
import by.egrius.pizzaShop.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserReadMapper userReadMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserReadDto createUser(UserCreateDto userCreateDto) {

        if (userRepository.existsByEmail(userCreateDto.email())) {
            throw new UserAlreadyExistsException(
                    "Пользователь с email " + userCreateDto.email() + " уже существует"
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
        log.info("Создан пользователь с ID: {}", savedUser.getId());

        return userReadMapper.map(savedUser);
    }


}
