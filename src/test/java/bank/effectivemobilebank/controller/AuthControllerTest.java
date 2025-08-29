package bank.effectivemobilebank.controller;

import bank.effectivemobilebank.model.User;
import bank.effectivemobilebank.model.UserRole;
import bank.effectivemobilebank.security.JwtService;
import bank.effectivemobilebank.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserService userService;

    @Test
    void loginSuccess() throws Exception {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("john");
        user.setPasswordHash("pass");
        user.setRoles(Set.of(UserRole.USER));

        when(userService.findByUsername("john")).thenReturn(Optional.of(user));
        when(jwtService.generate("john", java.util.Map.of("roles", user.getRoles())))
                .thenReturn("fake-jwt");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"john\", \"password\":\"pass\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake-jwt"));
    }

    @Test
    void loginFailureWrongCredentials() throws Exception {
        doThrow(new AuthenticationException("Bad credentials") {})
                .when(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"john\", \"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Неверные учетные данные"));
    }

    @Test
    void testRegister() throws Exception {
        UUID uuid = UUID.fromString("f427b7d0-6d42-4f1d-a442-ef6e4950537d");

        when(userService.createUser("john", "pass", Set.of(UserRole.USER)))
                .thenReturn(new User(uuid, "john", "pass", Set.of(UserRole.USER)));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"john\", \"password\":\"pass\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("f427b7d0-6d42-4f1d-a442-ef6e4950537d"));

    }

    @Test
    void registerUserAlreadyExists() throws Exception {
        when(userService.createUser("john", "pass", Set.of(UserRole.USER)))
                .thenThrow(new IllegalArgumentException("User already exists"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"john\", \"password\":\"pass\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("User already exists"));
    }

    @Test
    void loginNonExistentUser() throws Exception {
        doThrow(new AuthenticationException("Bad credentials") {})
                .when(authenticationManager).authenticate(any());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"noone\", \"password\":\"pass\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Неверные учетные данные"));
    }

    @Test
    void registerWithEmptyUsername() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\", \"password\":\"pass\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerWithEmptyPassword() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"john\", \"password\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginWithEmptyUsername() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\", \"password\":\"pass\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginWithEmptyPassword() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"john\", \"password\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginWithNullUsername() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"pass\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginWithNullPassword() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"john\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerWithNullUsername() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"pass\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerWithNullPassword() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"john\"}"))
                .andExpect(status().isBadRequest());
    }

}
