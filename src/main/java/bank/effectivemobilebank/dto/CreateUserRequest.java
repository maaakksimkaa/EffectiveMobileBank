package bank.effectivemobilebank.dto;

import bank.effectivemobilebank.model.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Setter
@Getter
public class CreateUserRequest {
    @NotBlank(message = "Имя пользователя обязательно")
    @Size(min = 3, max = 100, message = "Имя пользователя должно быть от 3 до 100 символов")
    private String username;
    
    @NotBlank(message = "Пароль обязателен")
    @Size(min = 6, message = "Пароль должен содержать минимум 6 символов")
    private String password;
    
    @NotNull(message = "Роли обязательны")
    private Set<UserRole> roles;

}


