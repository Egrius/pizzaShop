package by.egrius.pizzaShop.mapper.user;

import by.egrius.pizzaShop.dto.user.UserReadDto;
import by.egrius.pizzaShop.entity.User;
import by.egrius.pizzaShop.mapper.BaseMapper;
import org.springframework.stereotype.Component;

@Component
public class UserReadMapper implements BaseMapper<User, UserReadDto> {
    @Override
    public UserReadDto map(User object) {
        return new UserReadDto(
                object.getId(),
                object.getFullName(),
                object.getEmail(),
                object.getPhone(),
                object.getRole(),
                object.getCreatedAt(),
                object.getUpdatedAt()
        );
    }
}
