package by.egrius.pizzaShop.repository;

import by.egrius.pizzaShop.dto.pizza.PizzaCardDto;
import by.egrius.pizzaShop.entity.Pizza;
import by.egrius.pizzaShop.filter.PizzaFilter;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Repository
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class PizzaFilterRepositoryImpl implements PizzaFilterRepository {

    private final EntityManager entityManager;

    @Override
    public List<PizzaCardDto> findByFilter(PizzaFilter filter) {

        log.debug("Поиск пицц по фильтру: {}", filter);

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();

        CriteriaQuery<PizzaCardDto> criteriaQuery = criteriaBuilder.createQuery(PizzaCardDto.class);

        Root<Pizza> pizza = criteriaQuery.from(Pizza.class);

        List<Predicate> predicates= new ArrayList<>();

        predicates.add(criteriaBuilder.isTrue(pizza.get("available")));

        if(filter.name() != null && !filter.name().isEmpty()) {
            predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(pizza.get("name")),
                    "%" + filter.name().toLowerCase() + "%"
            ));
        }

        if(filter.description() != null && !filter.description().isEmpty()) {
            predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(pizza.get("description")),
                    "%" + filter.description().toLowerCase() + "%"
            ));
        }

        if(filter.category() != null && !filter.category().isEmpty()) {
            predicates.add(criteriaBuilder.like(pizza.get("category"), filter.category()));
        }

        if(filter.startPrice() != null && filter.startPrice().compareTo(BigDecimal.ZERO) > 0) {
            predicates.add(criteriaBuilder.equal(pizza.get("startPrice"), filter.startPrice()));
        }

        if (filter.fromPrice() != null && filter.toPrice() != null) {
            if (filter.fromPrice().compareTo(filter.toPrice()) > 0) {
                throw new IllegalArgumentException(
                        "Цена 'от' не может быть больше цены 'до'");
            }
        }

        if (filter.fromPrice() != null && filter.fromPrice().compareTo(BigDecimal.ZERO) > 0) {
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(pizza.get("startPrice"), filter.fromPrice()));
        }

        if (filter.toPrice() != null && filter.toPrice().compareTo(BigDecimal.ZERO) > 0) {
            predicates.add(criteriaBuilder.lessThanOrEqualTo(pizza.get("startPrice"), filter.toPrice()));
        }

        criteriaQuery.where(criteriaBuilder.and(predicates.toArray(Predicate[]::new)));

        criteriaQuery.select(criteriaBuilder.construct(
                PizzaCardDto.class,
                pizza.get("id"),
                pizza.get("name"),
                pizza.get("description"),
                pizza.get("imageUrl"),
                pizza.get("category"),
                pizza.get("startPrice")
        ));

        List<PizzaCardDto> result = entityManager.createQuery(criteriaQuery).getResultList();
        log.debug("Найдено {} пицц по фильтру", result.size());

        return result;
    }
}
