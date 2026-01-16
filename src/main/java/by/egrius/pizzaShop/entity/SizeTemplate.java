package by.egrius.pizzaShop.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "size_templates")
@Getter
@Setter(AccessLevel.PROTECTED)
@NoArgsConstructor
public class SizeTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "size_name", nullable = false, length = 20, unique = true)
    @Enumerated(EnumType.STRING)
    private PizzaSizeEnum sizeName;

    @Column(name = "display_name", length = 50)
    private String displayName;

    @Column(name = "diameter_cm")
    private Integer diameterCm;

    @Column(name = "weight_grams")
    private Integer weightGrams;

    @Column(name = "size_multiplier", precision = 4, scale = 2)
    private BigDecimal sizeMultiplier;

    public static SizeTemplate create(PizzaSizeEnum sizeName, String displayName, Integer diameterCm, Integer weightGrams, BigDecimal sizeMultiplier) {
        SizeTemplate sizeTemplate = new SizeTemplate();
        sizeTemplate.setSizeName(sizeName);
        sizeTemplate.setDisplayName(displayName);
        sizeTemplate.setDiameterCm(diameterCm);
        sizeTemplate.setWeightGrams(weightGrams);
        sizeTemplate.setSizeMultiplier(sizeMultiplier);
        return sizeTemplate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SizeTemplate that)) return false;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}