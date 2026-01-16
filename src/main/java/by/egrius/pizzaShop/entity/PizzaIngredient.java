package by.egrius.pizzaShop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Entity
@Table(name = "pizza_ingredients")
@Getter
@Setter
@NoArgsConstructor
public class PizzaIngredient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pizza_id", nullable = false)
    private Pizza pizza;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @Column(name = "weight_grams", nullable = false)
    private Integer weightGrams;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PizzaIngredient that)) return false;
        if (getId() != null && that.getId() != null) {
            return Objects.equals(getId(), that.getId());
        }

        // Вполне можно убрать, т.к добавлен сиквенс для айдишника пиццы...
        // Иначе - по бизнес-полям (натуральный ключ)
        return Objects.equals(getPizza(), that.getPizza()) &&
                Objects.equals(getIngredient(), that.getIngredient());
    }

    @Override
    public int hashCode() {
        if (getId() != null) {
            return Objects.hash(getId());
        }
        // Для новых/transient объектов
        return Objects.hash(
                getPizza() != null ? getPizza().getId() : null,
                getIngredient() != null ? getIngredient().getId() : null
        );
    }
}