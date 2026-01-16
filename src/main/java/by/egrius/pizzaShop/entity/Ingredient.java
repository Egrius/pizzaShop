package by.egrius.pizzaShop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Table(name = "ingredients")
@Entity
@Getter
@Setter
@ToString(exclude = "pizzaIngredients")
@NoArgsConstructor
public class Ingredient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    //, columnDefinition = "TEXT"
    @Column(nullable = false)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price; // pricePerKg

    @Column(name = "is_available")
    private boolean available = true;

    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "ingredient")
    private List<PizzaIngredient> pizzaIngredients;

    public static Ingredient create(String name, String description, BigDecimal price, boolean available) {
        Ingredient ingredient = new Ingredient();
        ingredient.setName(name);
        ingredient.setDescription(description);
        ingredient.setPrice(price);
        ingredient.setAvailable(available);
        return ingredient;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ingredient that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @PrePersist
    private void setCreatedAt() {
        this.createdAt = LocalDateTime.now();
    }
}