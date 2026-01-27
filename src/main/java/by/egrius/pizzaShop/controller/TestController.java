package by.egrius.pizzaShop.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/protected")
    public ResponseEntity<?> protectedEndpoint() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("User accessing protected endpoint: {}", auth.getName());

        return ResponseEntity.ok(
                String.format("Hello %s! This is protected endpoint. Your roles: %s",
                        auth.getName(),
                        auth.getAuthorities())
        );
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        return ResponseEntity.ok(
                Map.of(
                        "email", auth.getName(),
                        "authorities", auth.getAuthorities(),
                        "isAuthenticated", auth.isAuthenticated(),
                        "principal", auth.getPrincipal().getClass().getSimpleName()
                )
        );
    }
}
