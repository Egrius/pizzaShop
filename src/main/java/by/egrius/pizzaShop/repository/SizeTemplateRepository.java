package by.egrius.pizzaShop.repository;

import by.egrius.pizzaShop.entity.PizzaSizeEnum;
import by.egrius.pizzaShop.entity.SizeTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SizeTemplateRepository extends JpaRepository<SizeTemplate, Long> {
    boolean existsBySizeName(PizzaSizeEnum sizeName);
}
