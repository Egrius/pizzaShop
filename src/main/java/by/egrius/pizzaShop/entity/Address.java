package by.egrius.pizzaShop.entity;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    @NotNull
    private String street;

    @NotNull
    private String house;

    @NotNull
    private String apartment; // квартира/офис

    @NotNull
    private String entrance;

    @NotNull
    private String city;
}
