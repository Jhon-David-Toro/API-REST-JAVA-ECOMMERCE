package com.david.ecommerce_api.config;

import com.david.ecommerce_api.auth.Role;
import com.david.ecommerce_api.auth.User;
import com.david.ecommerce_api.auth.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Creates the first administrator from the ADMIN_EMAIL and ADMIN_PASSWORD variables when they are set
 * and no user with that email exists yet. Nothing is created when the variables are empty.
 */
@Component
@RequiredArgsConstructor
public class AdminSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.admin-name}")
    private String name;

    @Value("${app.seed.admin-email}")
    private String email;

    @Value("${app.seed.admin-password}")
    private String password;

    @Override
    public void run(ApplicationArguments args) {
        if (email.isBlank() || password.isBlank()) {
            return;
        }

        String normalizedEmail = User.normalizeEmail(email);

        if (!userRepository.existsByEmail(normalizedEmail)) {
            userRepository.save(User.create(name, normalizedEmail, passwordEncoder.encode(password), Role.ADMIN));
        }
    }
}
