package by.egrius.pizzaShop.repository;

import by.egrius.pizzaShop.dto.pizza.PizzaCardDto;
import by.egrius.pizzaShop.entity.Ingredient;
import by.egrius.pizzaShop.entity.Pizza;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PizzaRepository extends JpaRepository<Pizza, Long> {

    interface PizzaCardProjection {
        Long getId();
        String getName();
        String getDescription();
        String getImageUrl();
        String getCategory();
        BigDecimal getStartPrice();
    }

    interface PizzaUpdateProjection {
        Long getId();
        String getName();
        String getDescription();
        String getImageUrl();
        String getCategory();
        boolean isAvailable();
        Integer getCookingTimeMinutes();
    }

    @Query("""
            SELECT
            p.id as id,
            p.name as name,
            p.description as description,
            p.imageUrl as imageUrl,
            p.category as category,
            (SELECT MIN(ps.price)
                FROM PizzaSize ps
                WHERE ps.pizza = p AND ps.available = true) as startPrice
            FROM Pizza p
            WHERE p.available = true
            ORDER BY p.name
    """)
    Slice<PizzaCardProjection> findAvailablePizzasForCards(Pageable pageable);

    @EntityGraph(attributePaths = {
            "pizzaIngredients.ingredient",
            "pizzaSizes",
            "pizzaSizes.sizeTemplate"
    })
    @Query("SELECT p FROM Pizza p WHERE p.id = :id")
    Optional<Pizza> findPizzaDetails(@Param("id") Long id);

    @Query("SELECT p.id as id, " +
            "p.name as name, " +
            "p.description as description, " +
            "p.imageUrl as imageUrl, " +
            "p.category as category, " +
            "p.available as isAvailable, " +
            "p.cookingTimeMinutes as cookingTimeMinutes " +
            "FROM Pizza p WHERE p.id = :id")
    Optional<PizzaUpdateProjection> findByIdForUpdate(@Param("id") Long id);

    @Query("""
        SELECT p FROM Pizza p
        LEFT JOIN FETCH p.pizzaIngredients pi
        LEFT JOIN FETCH pi.ingredient
        LEFT JOIN FETCH p.pizzaSizes ps
         WHERE EXISTS (
             SELECT 1 FROM PizzaIngredient pi2
             WHERE pi2.pizza = p
             AND pi2.ingredient.id = :ingredientId
         )
    """)
    @QueryHints({
            @QueryHint(name = "org.hibernate.cacheable", value = "false")
    })
    List<Pizza> findPizzasByIngredientId(@Param("ingredientId") Long ingredientId);

    boolean existsByName(String name);

}

/*
SELECT JSON_OBJECT(
    'id': p.id,
    'name': p.name,
    'description': p.description,
    'imageUrl': p.image_url,
    'category': p.category,
    'cookingTimeMinutes': p.cooking_time_minutes,
    'ingredients': (
        SELECT JSON_ARRAYAGG(JSON_OBJECT('name': i.name))
        FROM pizza_ingredients pi
        JOIN ingredients i ON pi.ingredient_id = i.id
        WHERE pi.pizza_id = p.id
    ),
    'sizes': (
        SELECT JSON_ARRAYAGG(JSON_OBJECT(
            'sizeName': st.size_name,
            'displayName': st.display_name,
            'diameterCm': st.diameter_cm,
            'weightGrams': st.weight_grams,
            'price': ps.price
        ))
        FROM pizza_sizes ps
        JOIN size_templates st ON ps.size_template_id = st.id
        WHERE ps.pizza_id = p.id
        ORDER BY st.diameter_cm
    )
) as pizza_json
FROM pizzas p
WHERE p.id = :id
 */