package by.egrius.pizzaShop.repository;

import by.egrius.pizzaShop.entity.Pizza;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PizzaRepository extends JpaRepository<Pizza, Long> {

    boolean existsByName(String name);
}
