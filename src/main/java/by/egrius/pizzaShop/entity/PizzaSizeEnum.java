package by.egrius.pizzaShop.entity;

public enum PizzaSizeEnum {
    SMALL("25см"), MEDIUM("30см"), LARGE("35см");

    private String abbreviation;

     PizzaSizeEnum(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getAbbreviation() {
        return abbreviation;
    }
}
