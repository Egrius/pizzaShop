package by.egrius.pizzaShop.mapper.pizza_size;

import by.egrius.pizzaShop.dto.pizza_size.PizzaSizeReadDto;
import by.egrius.pizzaShop.entity.PizzaSize;
import by.egrius.pizzaShop.mapper.BaseMapper;
import by.egrius.pizzaShop.mapper.size_template.SizeTemplateReadMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PizzaSizeReadMapper implements BaseMapper<PizzaSize, PizzaSizeReadDto> {

    private final SizeTemplateReadMapper sizeTemplateReadMapper;

    @Override
    public PizzaSizeReadDto map(PizzaSize object) {
        return new PizzaSizeReadDto(
                object.getId(),
                sizeTemplateReadMapper.map(object.getSizeTemplate()),
                object.getPrice(),
                object.isAvailable()
        );
    }
}
