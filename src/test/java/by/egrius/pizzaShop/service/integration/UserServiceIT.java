package by.egrius.pizzaShop.service.integration;

import by.egrius.pizzaShop.mapper.UserReadMapper;
import by.egrius.pizzaShop.service.UserService;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({UserService.class, UserReadMapper.class, PasswordEncoder.class})
class UserServiceIT {

}