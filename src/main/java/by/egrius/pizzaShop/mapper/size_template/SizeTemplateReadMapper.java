package by.egrius.pizzaShop.mapper.size_template;

import by.egrius.pizzaShop.dto.size_template.SizeTemplateReadDto;
import by.egrius.pizzaShop.entity.SizeTemplate;
import by.egrius.pizzaShop.mapper.BaseMapper;
import org.springframework.stereotype.Component;

@Component
public class SizeTemplateReadMapper implements BaseMapper<SizeTemplate, SizeTemplateReadDto> {
    @Override
    public SizeTemplateReadDto map(SizeTemplate object) {
        return new SizeTemplateReadDto(
                object.getId(),
                object.getSizeName(),
                object.getDisplayName(),
                object.getDiameterCm(),
                object.getWeightGrams(),
                object.getSizeMultiplier()
        );
    }
}
