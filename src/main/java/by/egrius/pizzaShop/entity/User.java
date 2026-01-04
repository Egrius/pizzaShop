package by.egrius.pizzaShop.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Objects;

@Table(name = "users")
@Getter
@Setter(AccessLevel.PROTECTED)
@NoArgsConstructor
@ToString
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(nullable = false, unique = true, length = 15)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private UserRole role = UserRole.CUSTOMER;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false)
    private LocalDateTime updatedAt;

    public static User createUser(String fullName, String email, String phone, String passwordHash) {
        User user = new User();
        user.fullName = fullName.trim();
        user.email = email.toLowerCase().trim();
        user.phone = phone.trim();
        user.passwordHash = passwordHash;
        return user;
    }

    public void changeFullName(String newName) {
        this.fullName = newName.trim();
    }

    public void changePhone(String newPhone) {
        this.phone = newPhone.trim();
    }

    public void changePassword(String rawPassword, PasswordEncoder encoder) {
        this.passwordHash = encoder.encode(rawPassword);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

    }


}