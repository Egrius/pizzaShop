package by.egrius.pizzaShop.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "pizzas")
@Getter
@Setter
@ToString(exclude = {"ingredients"})
@NoArgsConstructor
public class Pizza {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
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

    @ManyToMany
    @JoinTable(
            name = "pizza_ingredients",
            joinColumns = @JoinColumn(name = "pizza_id"),
            inverseJoinColumns = @JoinColumn(name = "ingredient_id")
    )
    private List<Ingredient> ingredients;

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