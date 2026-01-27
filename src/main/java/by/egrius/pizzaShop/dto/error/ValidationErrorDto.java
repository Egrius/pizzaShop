package by.egrius.pizzaShop.dto.error;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ValidationErrorDto {
    List<ViolationDto> violations = new ArrayList<>();
}
