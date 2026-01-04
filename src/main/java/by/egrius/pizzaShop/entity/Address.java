package by.egrius.pizzaShop.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    private String street;
    private String house;
    private String apartment; // квартира/офис
    private String entrance;
    private String floor;
    private String city;
}
