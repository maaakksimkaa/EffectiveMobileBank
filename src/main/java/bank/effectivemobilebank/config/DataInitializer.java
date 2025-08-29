package bank.effectivemobilebank.config;

import bank.effectivemobilebank.model.UserRole;
import bank.effectivemobilebank.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Set;

@Configuration
@Profile("!test")
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserService userService) {
        return args -> {
            if (userService.findByUsername("admin").isEmpty()) {
                userService.createUser("admin", "admin123", Set.of(UserRole.ADMIN));
                System.out.println("Администратор создан: admin/admin123");
            }

            if (userService.findByUsername("user").isEmpty()) {
                userService.createUser("user", "user123", Set.of(UserRole.USER));
                System.out.println("Пользователь создан: user/user123");
            }
        };
    }
}


