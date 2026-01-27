package by.egrius.pizzaShop.repository;

import by.egrius.pizzaShop.entity.Pizza;
import by.egrius.pizzaShop.entity.PizzaIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PizzaIngredientRepository extends JpaRepository<PizzaIngredient, Long> {

    @Query("SELECT COUNT(pi) FROM PizzaIngredient pi WHERE pi.ingredient.id = :ingredientId")
    long countPizzaUses(@Param("ingredientId") Long ingredientId);

    @Query("SELECT pi FROM PizzaIngredient pi WHERE pi.pizza.id = :id")
    List<PizzaIngredient> findByPizzaId(@Param("id")Long id);
}