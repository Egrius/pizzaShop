package by.egrius.pizzaShop.repository;

import by.egrius.pizzaShop.entity.PizzaIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PizzaIngredientRepository extends JpaRepository<PizzaIngredient, Long> {

    @Query("SELECT pi FROM PizzaIngredient pi WHERE pi.pizza.id = :id")
    Optional<PizzaIngredient> findByPizzaId(@Param("id")Long id);
}