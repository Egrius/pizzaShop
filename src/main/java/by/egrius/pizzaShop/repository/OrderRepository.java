package by.egrius.pizzaShop.repository;

import by.egrius.pizzaShop.entity.Order;
import by.egrius.pizzaShop.entity.OrderStatus;
import by.egrius.pizzaShop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByUser(User user);

    @Query("SELECT o FROM Order o WHERE o.status = :status")
    List<Order> findAllByStatus(@Param("status") OrderStatus status);
}