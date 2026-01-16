package by.egrius.pizzaShop.repository;

import by.egrius.pizzaShop.entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    @Query("SELECT COUNT(pi) FROM PizzaIngredient pi WHERE pi.ingredient.id = :ingredientId")
    long countPizzaUses(@Param("ingredientId") Long ingredientId);

    boolean existsByName(String name);
}
