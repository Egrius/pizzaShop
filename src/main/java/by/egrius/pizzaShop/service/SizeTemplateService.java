package by.egrius.pizzaShop.service;

import by.egrius.pizzaShop.dto.size_template.SizeTemplateCreateDto;
import by.egrius.pizzaShop.dto.size_template.SizeTemplateReadDto;
import by.egrius.pizzaShop.entity.SizeTemplate;
import by.egrius.pizzaShop.mapper.size_template.SizeTemplateCreateMapper;
import by.egrius.pizzaShop.mapper.size_template.SizeTemplateReadMapper;
import by.egrius.pizzaShop.repository.PizzaSizeRepository;
import by.egrius.pizzaShop.repository.SizeTemplateRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SizeTemplateService {
    private final PizzaSizeRepository pizzaSizeRepository;
    private final SizeTemplateRepository sizeTemplateRepository;
    private final SizeTemplateCreateMapper sizeTemplateCreateMapper;
    private final SizeTemplateReadMapper sizeTemplateReadMapper;

    public List<SizeTemplateReadDto> getAllSizeTemplates() {
        return sizeTemplateRepository.findAll().stream().map(sizeTemplateReadMapper::map).toList();
    }

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

    public void deleteSizeTemplate(Long id) {
        log.info("Передано id на удаление шаблона размера - {}", id);

        SizeTemplate sizeTemplateToDelete = sizeTemplateRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Шаблон размера с id - " + id + " не найден"));

        long sizeTemplateUsages = pizzaSizeRepository.countSizeTemplateUsages(id);

        if(sizeTemplateUsages > 0) {
            throw new IllegalStateException(
                    String.format("Невозможно удалить шаблон размера '%s', так как он используется в %d пиццах",
                            sizeTemplateToDelete.getDisplayName(), sizeTemplateUsages));
        }

        String sizeTemplateToDeleteName = sizeTemplateToDelete.getDisplayName();

        log.info("Найден шаблон размера для удаления, id - {}, название - {}", id, sizeTemplateToDeleteName);

        sizeTemplateRepository.delete(sizeTemplateToDelete);

        log.info("Ингредиент с id - {}, название - {}, был успешно удален", id, sizeTemplateToDeleteName);
    }
}