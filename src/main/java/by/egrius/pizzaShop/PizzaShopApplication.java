package by.egrius.pizzaShop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PizzaShopApplication {

	public static void main(String[] args) {
		SpringApplication.run(PizzaShopApplication.class, args);

	}
}

// TODO Тесты для нового контроллера (unit, IT)
// TODO Контроллер для шаблонов размеров
// TODO Разобраться с кэшированием и попробовать тесты на производительность (возможно стоит после готового апи)
// TODO Система заказов
// TODO Система отзывов