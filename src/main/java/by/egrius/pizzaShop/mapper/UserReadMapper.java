package by.egrius.pizzaShop.mapper;

import by.egrius.pizzaShop.dto.UserReadDto;
import by.egrius.pizzaShop.entity.User;
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
                object.getRole().getName(),
                object.getCreatedAt(),
                object.getUpdatedAt()
        );
    }
}
