package by.egrius.pizzaShop.repository;

import by.egrius.pizzaShop.entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    @Query("SELECT COUNT(i) FROM Ingredient i WHERE i.name = :name")
    long countByName(@Param("name") String name);

    Optional<Ingredient> findByName(String name);

    boolean existsByName(String name);
}
