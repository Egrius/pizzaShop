package by.egrius.pizzaShop.service;

import by.egrius.pizzaShop.dto.size_template.SizeTemplateCreateDto;
import by.egrius.pizzaShop.entity.SizeTemplate;
import by.egrius.pizzaShop.mapper.size_template.SizeTemplateCreateMapper;
import by.egrius.pizzaShop.repository.SizeTemplateRepository;
import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class SizeTemplateService {
    private final SizeTemplateRepository sizeTemplateRepository;
    private final SizeTemplateCreateMapper sizeTemplateCreateMapper;

    public void createSizeTemplate(SizeTemplateCreateDto sizeTemplateCreateDto) {
        log.info("Передан DTO для создания размера \"{}\"", sizeTemplateCreateDto.sizeName().getAbbreviation());

        if(sizeTemplateRepository.existsBySizeName(sizeTemplateCreateDto.sizeName())) {
            log.info("Попытка создать существующий размер {}",  sizeTemplateCreateDto.sizeName().getAbbreviation());
            throw new EntityExistsException("Размер " + sizeTemplateCreateDto.sizeName().getAbbreviation() + " уже существует в БД");
        }

        SizeTemplate sizeTemplate = sizeTemplateCreateMapper.map(sizeTemplateCreateDto);

        sizeTemplateRepository.save(sizeTemplate);
        log.info("Новый размер успешно сохранён в БД");
    }
}
