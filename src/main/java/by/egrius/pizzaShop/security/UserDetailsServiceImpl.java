package by.egrius.pizzaShop.security;

import by.egrius.pizzaShop.entity.User;
import by.egrius.pizzaShop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Загрузка пользователя по email: {}", email);

        User user = userRepository.findByEmail(email).orElseThrow(() -> {
            log.warn("Не найден пользователь по email: {}", email);

            return new UsernameNotFoundException("Не найден пользователь с email: " + email);
        });
        log.debug("Пользователь найден: {} {}", user.getFullName(), user.getEmail());
        return new UserDetailsImpl(user);
    }
}
