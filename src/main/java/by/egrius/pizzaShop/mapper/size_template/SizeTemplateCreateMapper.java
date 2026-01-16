package by.egrius.pizzaShop.mapper.size_template;

import by.egrius.pizzaShop.dto.size_template.SizeTemplateCreateDto;
import by.egrius.pizzaShop.entity.SizeTemplate;
import by.egrius.pizzaShop.mapper.BaseMapper;
import org.springframework.stereotype.Component;

@Component
public class SizeTemplateCreateMapper implements BaseMapper<SizeTemplateCreateDto, SizeTemplate> {
    @Override
    public SizeTemplate map(SizeTemplateCreateDto object) {
        return SizeTemplate.create(
                object.sizeName(),
                object.displayName(),
                object.diameterCm(),
                object.weightGrams(),
                object.sizeMultiplier()
        );
    }
}
