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
            seed(users, encoder, environment, "SEED_USER_EMAIL", "SEED_USER_PASSWORD", "HNHSTORE User", userRole);
            seed(users, encoder, environment, "SEED_ADMIN_EMAIL", "SEED_ADMIN_PASSWORD", "Hoàng Ngọc Huy", adminRole);
        };
    }

    private void seed(UserRepository users, PasswordEncoder encoder, Environment env,
                      String emailKey, String passwordKey, String name, Role role) {
        String email = env.getProperty(emailKey);
        String password = env.getProperty(passwordKey);
        if (email != null && !email.isBlank() && password != null && !password.isBlank()
                && !users.existsByEmailIgnoreCase(email)) {
            users.save(new User(email.toLowerCase(), encoder.encode(password), name, role));
        }
    }
}
