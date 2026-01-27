package by.egrius.pizzaShop.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "pizza_sizes")
@Getter
@Setter(AccessLevel.PROTECTED)
@ToString(exclude = "pizza")
@NoArgsConstructor
public class PizzaSize {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pizza_id", nullable = false)
    private Pizza pizza;

    @ManyToOne
    @JoinColumn(name = "size_template_id")
    private SizeTemplate sizeTemplate;

    @Setter
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "is_available")
    private boolean available = true;

    public static PizzaSize create(Pizza pizza, SizeTemplate sizeTemplate, BigDecimal price, boolean available) {
        PizzaSize pizzaSize = new PizzaSize();

        pizzaSize.setPizza(pizza);
        pizzaSize.setSizeTemplate(sizeTemplate);
        pizzaSize.setPrice(price);
        pizzaSize.setAvailable(available);

        return pizzaSize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PizzaSize that)) return false;

        // Если оба persistent, можно сравнить по ID для производительности
        if (getId() != null && that.getId() != null) {
            return Objects.equals(getId(), that.getId());
        }

        // Иначе - по бизнес-полям (натуральный ключ)
        return Objects.equals(getPizza(), that.getPizza()) &&
                Objects.equals(getSizeTemplate(), that.getSizeTemplate());
    }

    @Override
    public int hashCode() {
        if (getId() != null) {
            return Objects.hash(getId());
        }
        // Для новых/transient объектов
        return Objects.hash(
                getPizza() != null ? getPizza().getId() : null,
                getSizeTemplate() != null ? getSizeTemplate().getId() : null
        );
    }
}
