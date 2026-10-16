package by.egrius.pizzaShop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PizzaShopApplication {

	public static void main(String[] args) {
		SpringApplication.run(PizzaShopApplication.class, args);

	}
}

// TODO оптимизация запросов, кэш

/*
services:
  pizza-app:
    build: .
    ports:
      - target: 8080
        published: 8080
    environment:
      DB_HOST:
      DB_PORT: 5432
      DB_NAME: pizza_db
      DB_USER: pizza_user
      DB_PASSWORD: pizza123

    depends_on:
      - postgres
        redis

  postgres:
    image: postgres
    environment:
      POSTGRES_USER: pizzaUser
      POSTGRES_PASSWORD: pizza123

  redis:
    image: redis

    environment:
      REDIS_HOST:
      REDIS_PORT:

networks:
 */