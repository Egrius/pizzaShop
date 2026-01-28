package by.egrius.pizzaShop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// TODO Сделать кастомный валидатор для PizzaUpdateDto (поддержка частичного обновления + валидация данных)
// TODO Валидировать на уровне контроллера

@SpringBootApplication
public class PizzaShopApplication {

	public static void main(String[] args) {
		SpringApplication.run(PizzaShopApplication.class, args);

	}
}