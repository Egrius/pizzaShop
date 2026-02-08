package by.egrius.pizzaShop.repository;

import by.egrius.pizzaShop.entity.Order;
import by.egrius.pizzaShop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByUser(User user);
}