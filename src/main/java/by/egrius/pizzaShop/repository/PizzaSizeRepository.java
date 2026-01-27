package by.egrius.pizzaShop.repository;

import by.egrius.pizzaShop.entity.PizzaSize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PizzaSizeRepository extends JpaRepository<PizzaSize, Long> {

    @Query("SELECT ps FROM PizzaSize ps WHERE ps.pizza.id = :id")
    List<PizzaSize> findByPizzaId(@Param("id")Long id);

    @Query("SELECT COUNT(st) FROM SizeTemplate st WHERE st.id = :sizeTemplateId")
    long countSizeTemplateUsages(@Param("sizeTemplateId") Long sizeTemplateId);
}