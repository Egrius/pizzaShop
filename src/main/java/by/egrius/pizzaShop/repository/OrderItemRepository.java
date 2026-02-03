package by.egrius.pizzaShop.repository;

import by.egrius.pizzaShop.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.yaml.snakeyaml.events.Event;

public interface OrderItemRepository extends JpaRepository<OrderItem, Event.ID> {
}
