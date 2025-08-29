package bank.effectivemobilebank.controller;

import bank.effectivemobilebank.model.User;
import bank.effectivemobilebank.model.UserRole;
import bank.effectivemobilebank.security.JwtService;
import bank.effectivemobilebank.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userService = userService;
    }

    public record AuthRequest(@NotBlank String username, @NotBlank String password) {}

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(401).body(Map.of("message", "Неверные учетные данные"));
        }
        var user = userService.findByUsername(request.username()).orElseThrow();
        var token = jwtService.generate(user.getUsername(), Map.of("roles", user.getRoles()));
        return ResponseEntity.ok(Map.of("token", token));
    }

    public record RegisterRequest(@NotBlank String username, @NotBlank String password) {}

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.createUser(request.username(), request.password(), Set.of(UserRole.USER));
        return ResponseEntity.ok(Map.of("id", user.getId()));
    }
}


