package vn.hnhstore.user;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DemoUsers {
    @Bean
    CommandLineRunner seedDemoUsers(RoleRepository roles, UserRepository users,
                                    PasswordEncoder encoder, Environment environment) {
        return args -> {
            Role userRole = roles.findByName("USER").orElseGet(() -> roles.save(new Role("USER")));
            Role adminRole = roles.findByName("ADMIN").orElseGet(() -> roles.save(new Role("ADMIN")));
            seed(users, encoder, environment, "SEED_USER_EMAIL", "SEED_USER_PASSWORD",
                    "SEED_USER_USERNAME", "user01", "HNHSTORE User", userRole);
            seed(users, encoder, environment, "SEED_ADMIN_EMAIL", "SEED_ADMIN_PASSWORD",
                    "SEED_ADMIN_USERNAME", "huyadmin", "Hoàng Ngọc Huy", adminRole);
        };
    }

    private void seed(UserRepository users, PasswordEncoder encoder, Environment env,
                      String emailKey, String passwordKey, String usernameKey,
                      String defaultUsername, String name, Role role) {
        String email = env.getProperty(emailKey);
        String password = env.getProperty(passwordKey);
        String username = env.getProperty(usernameKey, defaultUsername);
        if (email != null && !email.isBlank() && password != null && !password.isBlank()
                && !users.existsByEmailIgnoreCase(email)) {
            if (username.isBlank() || username.contains("@") || users.existsByUsernameIgnoreCase(username)) {
                throw new IllegalStateException("Demo username is invalid or already used");
            }
            users.save(new User(username.toLowerCase(), email.toLowerCase(),
                    encoder.encode(password), name, null, role));
        }
    }
}
