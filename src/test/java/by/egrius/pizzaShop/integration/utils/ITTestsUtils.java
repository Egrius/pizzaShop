package by.egrius.pizzaShop.integration.utils;

import by.egrius.pizzaShop.entity.User;
import by.egrius.pizzaShop.entity.UserRole;
import by.egrius.pizzaShop.repository.UserRepository;
import by.egrius.pizzaShop.security.UserDetailsServiceImpl;
import by.egrius.pizzaShop.security.jwt.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ITTestsUtils {

    private final UserDetailsServiceImpl userDetailsService;
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;


    public String setupUserAndGetJwtToken(String fullName, String email, String phone, String passwordHash, UserRole userRole) {
        userRepository.save(User.createUserCustom(fullName, email, phone, passwordHash, userRole));
        return generateToken(email);
    }

    private String generateToken(String email) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        return jwtUtils.generateJwtToken(authentication);
    }
}
