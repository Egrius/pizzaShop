package by.egrius.pizzaShop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "pizzas")
@Setter
@Getter
@ToString(exclude = {"pizzaIngredients", "pizzaSizes"})
@NoArgsConstructor
public class Pizza {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pizza_seq")
    @SequenceGenerator(name = "pizza_seq", sequenceName = "pizzas_id_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    //, columnDefinition = "TEXT"
    @Column(nullable = false)
    private String description;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(name = "is_available")
    private boolean available = true;

    @Column(name = "cooking_time_minutes")
    private Integer cookingTimeMinutes = 15;

    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "pizza", cascade = CascadeType.PERSIST)
    private Set<PizzaIngredient> pizzaIngredients = new HashSet<>();

    @OneToMany(mappedBy = "pizza", cascade = CascadeType.PERSIST)
    private Set<PizzaSize> pizzaSizes = new HashSet<>();

    @Version
    private Long version;

    public static Pizza create(String name, String description, String category,
                               String imageUrl, boolean available,
                               Integer cookingTimeMinutes) {
        Pizza pizza = new Pizza();
        pizza.setName(name);
        pizza.setDescription(description);
        pizza.setCategory(category);
        pizza.setImageUrl(imageUrl);
        pizza.setAvailable(available);
        pizza.setCookingTimeMinutes(cookingTimeMinutes != null ? cookingTimeMinutes : 15);
        return pizza;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pizza pizza)) return false;
        return Objects.equals(getId(), pizza.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @PrePersist
    private void setCreatedAt() {
        this.createdAt = LocalDateTime.now();
    }

}