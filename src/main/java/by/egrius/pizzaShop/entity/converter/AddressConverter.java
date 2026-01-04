package by.egrius.pizzaShop.entity.converter;

import by.egrius.pizzaShop.entity.Address;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import tools.jackson.databind.ObjectMapper;

@Converter
public class AddressConverter implements AttributeConverter<Address, String> {
    public static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Address address) {
        if (address == null) {
            return null;
        }
        return objectMapper.writeValueAsString(address);
    }

    @Override
    public Address convertToEntityAttribute(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        return objectMapper.readValue(json, Address.class);
    }
}
