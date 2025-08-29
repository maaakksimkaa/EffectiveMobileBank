package bank.effectivemobilebank.controller;

import bank.effectivemobilebank.dto.CreateUserRequest;
import bank.effectivemobilebank.dto.UpdateUserRolesRequest;
import bank.effectivemobilebank.dto.UserDto;
import bank.effectivemobilebank.model.User;
import bank.effectivemobilebank.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {
    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody CreateUserRequest request) {
        try {
            User user = userService.createUser(request.getUsername(), request.getPassword(), request.getRoles());
            return ResponseEntity.ok(toUserDto(user));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Ошибка создания пользователя: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<User> users = userService.findAll();
        List<UserDto> userDtos = users.stream()
            .map(this::toUserDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(userDtos);
    }

    @PatchMapping("/{id}/roles")
    public ResponseEntity<UserDto> updateUserRoles(
            @PathVariable String id,
            @Valid @RequestBody UpdateUserRolesRequest request) {
        try {
            User user = userService.updateRoles(UUID.fromString(id), request.getRoles());
            return ResponseEntity.ok(toUserDto(user));
        } catch (Exception e) {
            throw new RuntimeException("Ошибка обновления ролей: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable String id) {
        try {
            userService.deleteById(UUID.fromString(id));
            return ResponseEntity.ok().body(Map.of("message", "Пользователь удален"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Ошибка удаления пользователя"));
        }
    }

    private UserDto toUserDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRoles(user.getRoles());
        return dto;
    }
}


