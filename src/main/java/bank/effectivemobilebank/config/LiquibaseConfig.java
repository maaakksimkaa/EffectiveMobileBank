package bank.effectivemobilebank.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "bank.effectivemobilebank.repository")
public class LiquibaseConfig {
}


