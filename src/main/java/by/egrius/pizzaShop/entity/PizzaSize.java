package by.egrius.pizzaShop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "pizza_sizes")
@Getter
@Setter
@ToString(exclude = "pizza")
@NoArgsConstructor
public class PizzaSize {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pizza_id", nullable = false)
    private Pizza pizza;

    @Column(name = "size_name", nullable = false, length = 20)
    private String sizeName;

    @Column(name = "diameter_cm", nullable = false)
    private Integer diameterCm;

    @Column(name = "weight_grams", nullable = false)
    private Integer weightGrams;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "is_available")
    private Boolean available = true;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PizzaSize pizzaSize)) return false;
        return Objects.equals(id, pizzaSize.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
