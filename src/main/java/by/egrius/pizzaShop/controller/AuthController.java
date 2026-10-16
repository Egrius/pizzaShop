package by.egrius.pizzaShop.controller;

import by.egrius.pizzaShop.dto.auth.AuthRequest;
import by.egrius.pizzaShop.dto.auth.AuthResponse;
import by.egrius.pizzaShop.dto.auth.RegisterResponse;
import by.egrius.pizzaShop.dto.user.UserCreateDto;
import by.egrius.pizzaShop.dto.user.UserReadDto;
import by.egrius.pizzaShop.entity.User;
import by.egrius.pizzaShop.security.UserDetailsImpl;
import by.egrius.pizzaShop.security.jwt.JwtUtils;
import by.egrius.pizzaShop.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "Register, login, logout")
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    /*
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        AuthResponse response = new AuthResponse(
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getFullName(),
                userDetails.getAuthorities().iterator().next().getAuthority(),
                ""
        );

        return ResponseEntity.ok(response);
    }

     */

    @Operation(summary = "Login", description = "Returns a JWT on success.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest authRequest) {

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.email().toLowerCase(),
                            authRequest.rawPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String jwtToken = jwtUtils.generateJwtToken(authentication);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            AuthResponse authResponse = new AuthResponse(
                    userDetails.getId(),
                    userDetails.getUsername(),
                    userDetails.getFullName(),
                    userDetails.getAuthorities().iterator().next().getAuthority(),
                    jwtToken,
                    "Login successful"
            );

            return ResponseEntity.ok(authResponse);

        } catch (Exception e) {
            log.error("Логин завершился неудачно для email: {}", authRequest.email(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Неправильный логин или пароль");
        }
    }

    @Operation(
            summary = "Register new customer",
            description = "Creates a CUSTOMER. Email and phone must be unique."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User created"),
            @ApiResponse(responseCode = "400", description = "Validation failed or user already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid UserCreateDto userCreateDto) {
        try {
            UserReadDto createdUser = userService.createUser(userCreateDto);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new RegisterResponse(
                            createdUser.id(),
                            createdUser.email(),
                            createdUser.fullName(),
                            "Регистрация успешна. Пожалуйста, войдите в систему."
                    ));

        } catch (Exception e) {
            log.error("Регистрация завершилась неудачно: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Ошибка регистрации: " + e.getMessage());
        }
    }

    @Operation(
            summary = "Logout",
            description = "Clears SecurityContext. JWT stays valid until expiry - no server-side blacklist."
    )
    @ApiResponse(responseCode = "200", description = "Logout successful")
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return ResponseEntity.ok("Logout successful");
    }
}