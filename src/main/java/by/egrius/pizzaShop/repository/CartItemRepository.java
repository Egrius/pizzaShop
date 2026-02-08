package by.egrius.pizzaShop.repository;

import by.egrius.pizzaShop.entity.CartItem;
import by.egrius.pizzaShop.entity.Pizza;
import by.egrius.pizzaShop.entity.PizzaSize;
import by.egrius.pizzaShop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    @Query("SELECT COUNT(c) FROM CartItem c WHERE c.user = :user")
    int countItemsByUser(@Param("user") User user);

    List<CartItem> findByUser(User user);

    Optional<CartItem> findByUserAndPizzaAndPizzaSize(
            User user, Pizza pizza, PizzaSize size);

    void deleteByUser(User user);

    int deleteByUserAndId(User user, Long id);

    boolean existsByUser(User user);

}