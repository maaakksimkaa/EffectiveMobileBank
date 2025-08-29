package bank.effectivemobilebank.dto;

import bank.effectivemobilebank.model.UserRole;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Setter
@Getter
public class UpdateUserRolesRequest {
    @NotNull(message = "Роли обязательны")
    @Size(min = 1, message = "Должна быть указана хотя бы одна роль")
    private Set<UserRole> roles;

}


