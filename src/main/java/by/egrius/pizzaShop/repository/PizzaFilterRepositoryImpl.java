package by.egrius.pizzaShop.repository;

import by.egrius.pizzaShop.dto.pizza.PizzaCardDto;
import by.egrius.pizzaShop.entity.Pizza;
import by.egrius.pizzaShop.entity.PizzaSize;
import by.egrius.pizzaShop.filter.PizzaFilter;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
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

        CriteriaQuery<PizzaCardDto> pizzaQuery = criteriaBuilder.createQuery(PizzaCardDto.class);
        Root<Pizza> pizza = pizzaQuery.from(Pizza.class);

        List<Predicate> pizzaPredicates= new ArrayList<>();

        pizzaPredicates.add(criteriaBuilder.isTrue(pizza.get("available")));

        if(filter.name() != null && !filter.name().isEmpty()) {
            pizzaPredicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(pizza.get("name")),
                    "%" + filter.name().toLowerCase() + "%"
            ));
        }

        if(filter.category() != null && !filter.category().isEmpty()) {
            pizzaPredicates.add(criteriaBuilder.like(pizza.get("category"), filter.category()));
        }

        boolean hasPriceFilter = true;

        if (filter.fromPrice() != null && filter.toPrice() != null) {
            if (filter.fromPrice().compareTo(filter.toPrice()) > 0) {
                throw new IllegalArgumentException(
                        "Цена 'от' не может быть больше цены 'до'");
            }
        } else {
            hasPriceFilter = false;
        }

        Subquery<BigDecimal> startPriceSubQuery = pizzaQuery.subquery(BigDecimal.class);
        Root<PizzaSize> startPriceRoot = startPriceSubQuery.from(PizzaSize.class);
        startPriceSubQuery.correlate(pizza);

        List<Predicate> startPricePredicates = new ArrayList<>();

        startPricePredicates.add(criteriaBuilder.equal(startPriceRoot.get("pizza").get("id"), pizza.get("id")));
        startPricePredicates.add(criteriaBuilder.isTrue(startPriceRoot.get("available")));

        startPriceSubQuery.where(criteriaBuilder.and(startPricePredicates.toArray(Predicate[]::new)));

        startPriceSubQuery.select(criteriaBuilder.min(startPriceRoot.get("price")));

        Subquery<BigDecimal> priceFilterSubQuery = pizzaQuery.subquery(BigDecimal.class);
        Root<PizzaSize> priceFilterRoot = priceFilterSubQuery.from(PizzaSize.class);
        priceFilterSubQuery.correlate(pizza);

        List<Predicate> priceFilterPredicates = new ArrayList<>();
        priceFilterPredicates.add(criteriaBuilder.isTrue(priceFilterRoot.get("available")));
        priceFilterPredicates.add(criteriaBuilder.equal(priceFilterRoot.get("pizza").get("id"), pizza.get("id")));


        if(filter.fromPrice() != null && filter.fromPrice().compareTo(BigDecimal.ZERO) > 0) {
            priceFilterPredicates.add(criteriaBuilder.greaterThanOrEqualTo(priceFilterRoot.get("price"), filter.fromPrice()));
        }

        if(filter.toPrice() != null && filter.toPrice().compareTo(BigDecimal.ZERO) > 0) {
            priceFilterPredicates.add(criteriaBuilder.lessThanOrEqualTo(priceFilterRoot.get("price"), filter.toPrice()));
        }

        priceFilterSubQuery.where(criteriaBuilder.and(priceFilterPredicates.toArray(Predicate[]::new)));
        priceFilterSubQuery.select(criteriaBuilder.literal(BigDecimal.valueOf(1L)));

        if (hasPriceFilter) {
            pizzaPredicates.add(criteriaBuilder.exists(priceFilterSubQuery));
        }

        pizzaQuery.where(criteriaBuilder.and(pizzaPredicates.toArray(Predicate[]::new)));

        pizzaQuery.select(criteriaBuilder.construct(
                PizzaCardDto.class,
                pizza.get("id"),
                pizza.get("name"),
                pizza.get("description"),
                pizza.get("imageUrl"),
                pizza.get("category"),
                startPriceSubQuery
        ));

        List<PizzaCardDto> result = entityManager.createQuery(pizzaQuery).getResultList();
        log.debug("Найдено {} пицц по фильтру", result.size());

        return result;
    }
}
