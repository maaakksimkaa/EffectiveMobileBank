package bank.effectivemobilebank.dto;

import bank.effectivemobilebank.model.UserRole;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;

@Setter
@Getter
public class UserDto {
    private UUID id;
    private String username;
    private Set<UserRole> roles;
}


